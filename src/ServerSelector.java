import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerSelector implements Runnable {
    private final String EXIT_CMD = "-exit";//used to check if message recived from a client is exit
    private int n_activeConnection;
    private ThreadPoolExecutor workerPool;
    private ConcurrentHashMap<String, User> logged_user;
    private int port;
    private int BUFFER_DIMENSION;
    private DTStructure dati; //here there are all the data from users , post , basic functions etc

    public ServerSelector(int n_activeConnection, ServerConfig serverConfig, DTStructure dati) {
        this.n_activeConnection = n_activeConnection;
        this.BUFFER_DIMENSION = serverConfig.getBUFFER_DIMENSION();
        this.port = serverConfig.getTcpServerPort();
        this.dati = dati;
        this.logged_user = dati.getUserLogged();
    }

    @Override
    public void run() {
        try (
                ServerSocketChannel socketChannel = ServerSocketChannel.open() //open socketChannel
        ) {
            socketChannel.socket().bind(new InetSocketAddress(this.port)); //bind socketchannel to port passed by config
            //We set the socketChannel on to false blocking
            socketChannel.configureBlocking(false);
            //Open selector
            Selector sel = Selector.open();
            //then register on the selector the ACCEPT operation
            socketChannel.register(sel, SelectionKey.OP_ACCEPT);
            System.out.printf("Server: waiting for connection %d\n", this.port);

            //Creating CachedTrheadpool for handling threads
            workerPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            while (!Thread.currentThread().isInterrupted()) {
                //blocking operation. it blocks until one channel is selected.
                if (sel.select() == 0) {
                    continue;

                }
                //Set of keys corresponding to ready channels
                Set<SelectionKey> selectedKeys = sel.selectedKeys();
                // Set's iterator
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove(); //important to remove the element
                    try {
                        if (key.isAcceptable()) {               // ACCEPTABLE

                            // Accept a new connection using a SocketChannel for the
                            // communication with the client that request it

                            ServerSocketChannel server = (ServerSocketChannel) key.channel(); //return the channel for witch the key was created
                            SocketChannel client_Channel = server.accept();
                            client_Channel.configureBlocking(false);
                            System.out.println("Server: accepted new connection from client: " + client_Channel.getRemoteAddress());
                            System.out.printf("Server: number of open connection: %d\n", ++this.n_activeConnection);
                            this.registerRead(sel, client_Channel);//register a new connection
                        } else if (key.isReadable()) {        // READABLE
                            this.readClientMessage(sel, key);

                        } else if (key.isWritable()) {                 // WRITABLE
                            this.echoAnswer(sel, key);
                        }
                    } catch (IOException e) {
                        this.n_activeConnection--;
                        key.channel().close();
                        key.cancel();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("-----Server already active----\n\n-----Shutting off-----");
            //e.printStackTrace();
        }
    }

    /**
     *
     * @return true if thread pool has been interrupted correctly false otherwise
     */
    public boolean closeThreadPool(){
        if (workerPool==null) return true;
        workerPool.shutdown();
        try {
            while (!workerPool.isTerminated())
                workerPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param sel opened selector on witch are the connections
     * @param ClientChannel Socketchannel accepted
     * @throws IOException
     */
    private void registerRead(Selector sel, SocketChannel ClientChannel) throws IOException {

        //Buffer creation for length and message
        ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
        ByteBuffer message = ByteBuffer.allocate(BUFFER_DIMENSION);
        ByteBuffer[] bf = {length, message};
        //register the channel with the attachment
        ClientChannel.register(sel, SelectionKey.OP_READ, bf);
    }

    /**
     *
     * @param sel Selector
     * @param key key used to take socketChannel and read message
     * @throws IOException
     */
    private void readClientMessage(Selector sel, SelectionKey key) throws IOException {
        // Retrieve socketchannel from selectionKey
        SocketChannel c_channel = (SocketChannel) key.channel();
        // retrieve bytebuffer array (attachment).first length then message itself
        ByteBuffer[] bfs = (ByteBuffer[]) key.attachment();
        c_channel.read(bfs);
        if (!bfs[0].hasRemaining()) {//read 4 bytes as int for length
            bfs[0].flip();  //reset the pos = 0 on pos = 4 to read
            int l = bfs[0].getInt();//reading the int
            if (bfs[1].position() == l) {
                bfs[1].flip();
                String reply = new String(bfs[1].array()).trim();
                System.out.printf("Server: received %s\n", reply);
                String[] msg = reply.split(" ", 0);
                if (msg[0].equals(this.EXIT_CMD)) {
                    this.n_activeConnection--;
                    System.out.println("Server: close the connection with client " + c_channel.getRemoteAddress());
                    key.cancel();
                    c_channel.close();
                } else if (reply == null) {//if it's received an empty message
                    this.registerRead(sel, c_channel);
                } else { //otherwise, register with OP.Write, reply as attachment
                    c_channel.register(sel, SelectionKey.OP_WRITE, reply);
                }

            }
        }
    }

    /**
     *
     * @param sel Selector
     * @param key  key used to take socketChannel and write the response message
     * @throws IOException
     */
    private void echoAnswer(Selector sel, SelectionKey key) throws IOException {
        //Taking client's SocketChannel
         SocketChannel c_channel = (SocketChannel) key.channel();
        //retrieving attachment as string
        String req = (String) key.attachment();
        //create task to handle the request
        ReqHandler reqHandler = new ReqHandler(key, req);

        //use threadPool to solve the request and send the message to client
        this.workerPool.execute(reqHandler);
        //saving again the channel for next requests
        this.registerRead(sel, c_channel);
    }
}
