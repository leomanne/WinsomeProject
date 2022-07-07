import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class Client {

    //RMI variables
    private ClientInterface ci = null;  //object to export
    private ClientInterfaceImpl ciImpl = null;
    private ServerInterface serverInterface; //remote server object

    public SocketChannel client;//main connection (tcp) to server

    private Thread thread;//thread that listen to message from rewardHandler from server

    //local variables
    private final ClientConfig clientConfig; //Configuration of the client. if nothing is passed to ClientMain then used standard config
    public String nameUser;// name of the user after the login
    public volatile boolean  exit = false;
    private boolean logged = false;

    //CONSTRUCTOR
    public Client(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void start() {
        //RMI. Getting remote server object
        try {
            this.serverInterface = (ServerInterface) LocateRegistry.getRegistry(this.clientConfig.getRegistryAddr(), this.clientConfig.getRegistryPort()).lookup(this.clientConfig.getNameServer());
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Registry not found on Address and port specified");
            return;
        }
        try {
            //Open SocketChannel with WINSOMESERVER on address and port specified in clientConfig
            client = SocketChannel.open(new InetSocketAddress(clientConfig.getServerAddr(), clientConfig.getServerPort()));

            client.configureBlocking(true);//default true

            System.out.println("< Client: connected");
            System.out.println("< Press 'exit' to quit");
            System.out.println("< Press 'help' to see the usage");
            //bufferedreader to read from terminal
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            while (!this.exit) {
                String response;
                System.out.print("> ");

                String msg = reader.readLine();
                msg = "-" + msg;
                //args = msg splitted
                String[] args = msg.split(" ", 0);
                String args1 = args[0]+" ";
                if(args.length>1) {
                    args1 = args[0] + " " + args[1]+" ";
                }
                /******************************************/

                try {
                    /**
                     * Register Function -register <username> <password> <list of tags> (max 5)
                     *
                     */
                    if (args1.contains("-register ")) {
                        registerUser(args);
                    }


                    /**
                     * Login Function -login <username> <password>
                     */
                    else if (args1.contains("-login ")) {
                       loginUser(args,msg);
                    }


                    /**
                     * Logout Function -logout
                     */
                    else if (args1.contains("-logout ")) {
                        logoutUser(args,msg);
                    }


                    /**
                     * List user Function -list users
                     */
                    else if (args1.contains("-list users ")) {
                       listUsers(args,msg);
                    }
                    /**
                     * List followers Function -list followers
                     */
                    else if (args1.contains("-list followers ")) {
                        listFollowers();
                    }


                    /**
                     *List following Function -list following
                     */
                    else if(args1.contains("-list following ")){
                        listFollowing(args,msg);
                    }


                    /**
                     * Follow Function -follow <username>
                     */
                    else if (args1.contains("-follow ")) {
                        followUser(args,msg);
                    }

                    /**
                     * Unfollow Function -unfollow <username>
                     */
                    else if (args1.contains("-unfollow ")) {
                        unfollowUser(args,msg);
                    }


                    /**
                     * View Blog Function -view blog
                     */
                    else if (args1.contains("-blog ")) {
                        viewBlog(args,msg);
                    }

                    /**
                     * Post function -post "title" "content"
                     */
                    else if (args1.contains("-post ")) {
                        userPost(msg);
                    }


                    /**
                     * Show feed Function -show feed
                     */
                    else if (args1.contains("-show feed ")) {
                        showFeed(args,msg);
                    }


                    /**
                     * Show Post Function -show post <id>
                     */
                    else if (args1.contains("-show post ")) {
                        showPost(args,msg);
                    }


                    /**
                     * Delete Function -delete <idPost>
                     */
                    else if (args1.contains("-delete ")) {
                        deleteUserPost(args,msg);
                    }


                    /**
                     * Rewin Function -rewin <idPost>
                     */
                    else if (args1.contains("-rewin ")) {
                        rewinPost(args,msg);
                    }


                    /**
                     * Rate Function -rate <idPost> <vote> (+1,-1)
                     */
                    else if (args1.contains("-rate ")) {
                        ratePost(args,msg);
                    }


                    /**
                     * Comment function -comment <idPost> <comment>
                     */
                    else if (args1.contains("-comment ")) {
                        commentPost(msg);
                    }

                    /**
                     * GET wallet Bitcoin function -wallet btc
                     */
                    else if (args1.contains("-wallet btc ")) {
                        getWalletBTC(args,msg);
                    }
                    /**
                     * Transaction Function -wallet
                     */
                    else if (args1.contains("-wallet ")) {
                        getWalletUser(args,msg);
                    }

                    /***********************************/
                    //Some UTILITY FUNCTIONS

                    /**
                     * Exit function -exit
                     */
                    else if (args1.contains("-exit ")) {
                        this.exit = true;
                        if(this.nameUser!=null) {
                            String tmpMsg = "-logout";
                            String[] split = tmpMsg.split(" ",0);
                            this.logoutUser(split,tmpMsg);
                        }
                        this.sendMessage("-exit");
                    }
                    /**
                     * Help Function -help
                     */
                    else if (args1.contains("-help")) {
                        printUsage();
                    }else printUsage();

                } catch (Exception e) {
                    //System.out.println("< Error parsing command line arguments...");
                    e.printStackTrace();
                }
            }
            //exiting...
            chiusuraClient(client);

        } catch (IOException e) {
            System.out.println("\n< Client: closing");
        }

    }

    /**
     * @param args String request splitted
     * @param msg request as a string
     * @effect print the amount of the total wallet of a user in BTC currency
     */
    private void getWalletBTC(String[] args, String msg) {
        String response ;
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        }
        if(args.length==2) {
            this.sendMessage(msg.trim() + " " + this.nameUser);
            response = this.readMessage();
            System.out.println(response);
        }else {System.out.println("< Wrong input");}
    }

    /**
     * @param args String request splitted
     * @param msg request as a string -wallet
     * @effect print all the movement from the user's wallet and total amount
     */
    private void getWalletUser(String[] args, String msg) {
        String response ;
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        }
        if(args.length==1) {
            this.sendMessage(msg.trim() + " " + this.nameUser);
            response = this.readMessage();
            System.out.println(response);
        }else {System.out.println("< Wrong input");}
    }

    /**
     *
     * @param msg a string containing the request -comment <id> "title" "content"
     * @effect adds a comment on a certain post
     */
    private void commentPost(String msg) {
        String response ;
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        }
        this.sendMessage(msg.trim() +" "+this.nameUser);
        response = this.readMessage();
        System.out.println(response);

    }

    /**
     * @param args @msg.split
     * @param msg request as a string -rate <id> <+-1>
     * @effect adds a vote to a post
     */
    private void ratePost(String[] args, String msg) {
        msg = msg.replace("-","/");
        String response ;
        if(!logged){
            System.out.println("< User not logged!");
            return;
        }
        if(args.length!=3){
            System.out.println("< Wrong input");
            printUsage();
            return;

        }
        this.sendMessage(msg.trim()+" "+this.nameUser);
        response = this.readMessage();
        System.out.println(response);

    }

    /**
     * Rewin Function
     * @param args @msg.split
     * @param msg -rewin <postID>
     * @effect adds a certain post in user's feed on the user's blog
     */
    private void rewinPost(String[] args, String msg) {
        String response ;
        if(!logged){
            System.out.println("< User not logged!");
            return;
        }
        if(args.length!=2){
            System.out.println("< Wrong input");
            printUsage();
            return;
        }
        this.sendMessage(msg.trim() +" "+nameUser);
        response = this.readMessage();
        System.out.println(response);
    }

    /**
     *
     * @param args vector of string -delete <id>
     * @param msg @args as a string
     * @effect delete a post with <id> as id
     */
    private void deleteUserPost(String[] args, String msg) {
        String response ;
        if(!logged){
            System.out.println("< User not logged!");
            return;
        }
        if(args.length!=2){
            System.out.println("< Wrong input");
            printUsage();
            return;
        }
        this.sendMessage(msg.trim()+" "+this.nameUser);
        response = this.readMessage();
        System.out.println(response);
    }

    /**
     * @param args @msg splitted
     * @param msg request as a string -show post <id>
     * @effect print a post
     */
    private void showPost(String[] args, String msg) {
        String response ;
        for (String t: args) {
            System.out.println("{"+t+"}");
        }
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        }
        else if (args.length != 3) { //same if arguments are not enough or too many
            System.out.println("< Input Error");
            return;
        }
        this.sendMessage(msg.trim());
        response = this.readMessage();
        System.out.println("< -----------------------------------------");
        System.out.println(response);

    }

    /**
     *
     * @param args @msg.split
     * @param msg A string containing the request -show feed
     * @effects send -show feed + username to the server , and return a string containing all the post in the user's feed (ID author and title)
     */
    private void showFeed(String[] args, String msg) {
        String response ;
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        }
        if(args.length!=2){
            System.out.println("< Wrong input");
            printUsage();
            return;
        }
        this.sendMessage(msg.trim()+" "+this.nameUser);
        response = this.readMessage();
        System.out.println("------------------------------------------");
        System.out.println(response);
    }

    /**
     *
     * @param msg  String with che cli command -
     * @effects adds a post on user's blog, Receive the response from the server
     */
    private void userPost(String msg) {
        String response;
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        }

        this.sendMessage(msg.trim()+" "+this.nameUser);
        response = this.readMessage();
        System.out.println(response);
    }

    /**
     *
     * @param args String vector @msg split
     * @param msg  String with che cli command -view blog
     * @effects send and receive to and from Server a string containing the info about all the user's post to print
     */
    private void viewBlog(String[] args, String msg) {
        String response ;
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        } else if (args.length != 1) { //same if arguments are not enough or too many
            System.out.println("< Input Error");
            return;
        }
        this.sendMessage(msg.trim()+" "+nameUser);
        response = this.readMessage();
        System.out.println(response);
    }

    /**
     *
     * @param args String vector @msg split
     * @param msg  the request as a string -unfollow <username>
     * @effect unfollow a user
     */
    private void unfollowUser(String[] args, String msg) {
        String response ;
        if (!this.logged) {//if user already logged print error
            System.out.println("< User not Logged!");
            return;
        } else if (args.length != 2) { //same if arguments are not enough or too many
            System.out.println("< Input Error");
            return;
        }
        this.sendMessage(msg.trim() +" "+nameUser);
        response = this.readMessage();
        System.out.println("< "+response);
    }

    /**
     * @param args String vector @msg split
     * @param msg  the request as a string -follow <username>
     * @effect follow a users
     */
    private void followUser(String[] args, String msg) {
        String response;
        if(!logged){
            System.out.println("< User not logged!");
            return;
        }
        if (args.length!=2){
            System.out.println("< Wrong input...");
            return;
        }
        this.sendMessage(msg.trim()+" "+nameUser); //sending username to work in DTstructure
        response = this.readMessage();
        System.out.println(response);
    }

    /**
     *
     * @param args String vector @msg split
     * @param msg the request as a string -list following
     * @effect list the name of all the user who the user follows
     */
    private void listFollowing(String[] args, String msg) {
        String response ;
        if(!logged){
            System.out.println("< User not logged!");
            return;
        }
        if (args.length!=2){
            System.out.println("< Wrong input...");
            return;
        }
        this.sendMessage(msg.trim()+" "+nameUser); //sending username to work in DTstructure
        response = this.readMessage();
        System.out.println("< USERS\t| TAGS");
        System.out.println("< -----------------------------------------------");
        System.out.println(response);
    }

    /**
     * @effect list all the user's follower
     */
    private void listFollowers() {
        if (logged) {
            //Print list followers
            System.out.println("< User:");
            System.out.println("< ------------------------------------------------");
            try {
                //using remote methods to get all the usernames
                Set<String> tmp = this.ciImpl.getFollowers();
                for (String t: tmp) {
                    System.out.println("< "+t);
                }
            } catch (RemoteException e) {
                System.out.println("< Error while printing followers");
            }
        }else {
            System.out.println("< User not logged!!");
        }


    }

    /**
     *
     * @param args String vector @msg split
     * @param msg  String with che cli command -list users
     * @effect list all the users with at least 1 tag in common
     */
    private void listUsers(String[] args, String msg) {
        String response ;
        if(!logged){
            System.out.println("< User not logged!");
            return;
        }
        if (args.length!=2){
            System.out.println("< Wrong input...");
            printUsage();
            return;
        }
        this.sendMessage(msg.trim() +" "+ nameUser);
        response = this.readMessage();

        System.out.println("< USERS\t| TAGS");
        System.out.println("< -----------------------------------------------");
        System.out.println(response);
    }

    /**
     *
     * @param args String vector @msg split
     * @param msg  String with che cli command -logout
     * @effect logout from client
     */
    private void logoutUser(String[] args, String msg) {
        if(!this.logged){
            System.out.println("< User not logged");
            return;
        }
        String response ;
        if (args.length>1){
            System.out.println("< Too many arguments");
            return;
        }
        this.sendMessage(msg.trim()+" "+this.nameUser); //send <logout> + username <-it is used to remove from DTstructure the username
        response = this.readMessage();
        if (response.contains("ok logout")){
            this.logged = false;
            if (this.ci != null) {
                try {
                    this.serverInterface.unregisterForCallback(this.ci); //if user logged out unregister for callback
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //interrupt the server for the message from server mc
            thread.interrupt();
            System.out.println("< "+this.nameUser+" logged out");
            this.nameUser = null;
        }
    }

    /**
     *
     * @param args String vector @msg split
     * @param msg  String with che cli command -login <username> <password>
     * @effect login as username
     */
    private void loginUser(String[] args, String msg) {
        String response ;
        String mcAdd ;
        String mcPort;
        if (this.logged) {//if user already logged print error
            System.out.println("< User already logged!");
            return;
        } else if (args.length != 3) { //same if arguments are not enough or too many
            System.out.println("< Input Error");
            return;
        }
        this.sendMessage(msg.trim());
        response = this.readMessage();

        if (response.contains("success login")) {
            System.out.println("< logged in");
            this.logged = true;
            String[] tmp = response.split(" ", 0);

            this.nameUser = tmp[2];
            //taking MCaddress and MCPort from response
            mcAdd = tmp[3];
            mcPort = tmp[4];

            try {
                //Register for CALLBACK
                this.ciImpl = new ClientInterfaceImpl(this.nameUser);
                this.ci = (ClientInterface) UnicastRemoteObject.exportObject(this.ciImpl, 0);
                this.serverInterface.registerForCallback(this.ci);

            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("< Error Register for CallBack");
                //System.exit(0);
                //or softer
                this.logged=false;
                System.out.println("< logged out");
            }

            rewardMsgReceiver handler = new rewardMsgReceiver(mcAdd,mcPort);
            thread = new Thread(handler);
            //Start a thread to see notification from server
            thread.start();
        }else {
            System.out.println("< " + response);
        }

    }

    /**
     *
     * @param args vector of String with che cli command -register <username> <password> <tags>
     */
    private void registerUser(String[] args) {
        try {
            if (args.length < 4) {
                System.out.println("< Wrong input");
                return;
            }
            String name = args[1];
            String pwd = args[2];
            StringBuilder tags = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                tags.append(" ").append(args[i].trim());
            }
            //DO REGISTER
            if (tags.toString().split(" ", 0).length > 6) {
                System.out.println("< Wrong input");
                printUsage();
                return;
            }
            System.out.println("< --" + this.serverInterface.register(name, pwd, tags.toString().trim()) + "--");
        } catch (RemoteException e) {System.out.println("< Error in register");}
    }

    /**
     * @param client socketchannel connected to the server
     * @effects close the socketChannel connected to the server
     *
     */
    public void chiusuraClient(SocketChannel client) {
        try {
            if (client != null && client.isConnected())
                client.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("-----Server already closed-----");
        }
    }

    /**
     * @return a string containing the response from server on SocketChannel(tcp connection)
     */
    public String readMessage() {
        //READING THE MESSAGE FORWARDED FROM SERVER AS A BYTEBUFFER(STRING)
        String msgRead = null;
        int len;
        try {
            //the dimension of the buffer should suffice (we just send simple text) BUT we need testing
            ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
            client.read(length);
            length.clear();
            len = length.getInt();
            //System.out.println("["+len+"]");
            ByteBuffer reply = ByteBuffer.allocate(len);
            client.read(reply);
            reply.flip();
            String response = new String(reply.array()).trim();
            reply.clear();
            msgRead = response;

        } catch (IOException e) {
            this.exit=true;
        }
        return msgRead;
    }

    /**
     *
     * @param msg request to send to the server
     * @effect send this @msg on the SocketChannel
     */
    public void sendMessage(String msg) {
        try {
            if (client==null)return;
            ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
            length.putInt(msg.length());
            length.flip();
            client.write(length);
            length.clear();

            // Second part is the message itself
            ByteBuffer readBuffer = ByteBuffer.wrap(msg.getBytes());
            client.write(readBuffer);
            readBuffer.clear();
        } catch (IOException e) {
          this.exit=true;
        }
    }

    /**
     * @effect print the usage
     */
    private void printUsage() {

        System.out.println(
                        "\texit\t\t\t\t\t" +"| exit from the client\n"+
                        "\tregister <username> <password> <tags>\t" +"| register a certain user\n"+
                        "\tlogin <username> <password>\t\t" +"| login a certain user\n"+
                        "\tlogout\t\t\t\t\t" +"| logout from this user\n"+
                        "\tlist users\t\t\t\t" +"| list all of the user who have a tag in common\n"+
                        "\tlist followers\t\t\t\t" +"| list all of the users who follow this user\n"+
                        "\tlist following\t\t\t\t" +"| list all of the users who are followed by this user\n"+
                        "\tfollow <username>\t\t\t" +"| start to follow <username>\n"+
                        "\tunfollow <username>\t\t\t" +"| stop following <username>\n"+
                        "\tblog\t\t\t\t\t"+"| list all of the post on user's blog\n"+
                        "\tpost <title> <content> \t\t\t" +"| create a post with <title> and <content>\n"+
                        "\tshow feed\t\t\t\t" +"| list all of the post on user's feed\n"+
                        "\tshow post <id>\t\t\t\t" + "| show all info about a certain post\n"+
                        "\tdelete <idPost>\t\t\t\t" + "| if the user is the owner of the post delete it\n"+
                        "\trewin <idPost>\t\t\t\t"  + "| adds a post on user's blog\n"+
                        "\trate <idPost> <vote>\t\t\t" + "| vote a certain post \n"+
                        "\tcomment <idPost> <comment>\t\t" +"| comment a certain post\n"+
                        "\twallet\t\t\t\t\t" + "| show the transaction on user's wallet\n"+
                        "\twallet btc\t\t\t\t"+"| show the value of the wallet in BTC\n");
    }
}
