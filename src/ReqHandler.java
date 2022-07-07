import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReqHandler implements Runnable {
    //selectionKey used to get SocketChannel
    private SelectionKey key;
    //request to handle
    private String msg;
    public ReqHandler(SelectionKey key, String msg) {
        this.key = key;
        this.msg = msg;
    }

    @Override
    public void run() {
        String ans = DTStructure.handler(msg); //the static class will handle the request and send an answer
        System.out.println("Thread ricevuto : [" + this.msg + "] ... inviato: [" + ans + "]"); //TESTING
        // prepare the response
        ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
        length.putInt(ans.length());
        length.flip();
        ByteBuffer message = ByteBuffer.wrap(ans.getBytes());
        // toSend will be used as the last buffer to send to client
        ByteBuffer toSend = ByteBuffer
                .allocate(length.remaining() + message.remaining())
                .put(length)
                .put(message);
        toSend.flip();  // doing this will make it ready to be read
        length.clear();

        SocketChannel c_channel = (SocketChannel) key.channel();//taking the client channel to write on it

        try {
            c_channel.write(toSend);
            while (toSend.hasRemaining()) {
                c_channel.write(toSend);
            }
        } catch (IOException e) {
        }

    }
}
