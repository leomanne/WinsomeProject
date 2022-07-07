import java.io.IOException;
import java.net.*;


public class RewardHandler implements Runnable {
    //MCPort mcaddress taken from Configuration file
    private int mcPort;
    private String mcAddress;

    private long interval;
    //represent an ipaddress
    private InetAddress multicastGroup;

    private String msg;

    public RewardHandler(int mcPort, String mcAddress, long interval) {
        this.mcAddress = mcAddress;
        this.mcPort = mcPort;
        this.interval = interval;
        this.msg = "Finito di calcolare il reward...";
    }

    @Override
    public void run() {
        DatagramSocket datagramSocket = null; //used to send the message MC
        try {
            this.multicastGroup = InetAddress.getByName(mcAddress);
            if (!this.multicastGroup.isMulticastAddress()) { //check if the ip passed is correct
                throw new IllegalArgumentException();
            }
            try (DatagramSocket sock = new DatagramSocket()) {
                while (!Thread.currentThread().isInterrupted()) {
                    //create the packet to send
                    DatagramPacket dat = new DatagramPacket(
                            msg.getBytes(),
                            msg.length(),
                            this.multicastGroup,
                            this.mcPort
                    );
                    DTStructure.reward();
                    DTStructure.BTCReward();
                    //send message to clients
                    sock.send(dat);
                    Thread.sleep(this.interval);
                }
                DTStructure.reward();
                DTStructure.BTCReward();
            } catch (IOException | InterruptedException e) {
                //e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}