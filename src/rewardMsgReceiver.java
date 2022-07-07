import java.io.IOException;
import java.net.*;

//thread used by CLIENT to receive the MC messages by rewardHandler thread
public class rewardMsgReceiver implements Runnable {
    //port and address used for MC taken by login response
    private int mcport;
    private String mcAdd;

    private InetAddress welcomeGroup;
    private final int MSG_LENGTH = 1024;
    public rewardMsgReceiver(String mcAdd, String mcPort) {
        this.mcport=Integer.parseInt(mcPort);
        this.mcAdd= mcAdd;
    }

    @Override
    public void run() {

        try{
            this.welcomeGroup = InetAddress.getByName(mcAdd);//get the InetAddress

            if (!this.welcomeGroup.isMulticastAddress()) {//if not valid address
                throw new IllegalArgumentException();
            }
            try (MulticastSocket multicastWelcome = new MulticastSocket(this.mcport)) {//bind the multicast socket on the MC port
                multicastWelcome.joinGroup(this.welcomeGroup); //join group
               while(!Thread.currentThread().isInterrupted()){
                    DatagramPacket dat = new DatagramPacket(new byte[this.MSG_LENGTH], MSG_LENGTH);
                    //receive the packet sent
                    multicastWelcome.receive(dat);
                    /* System.out.print("> ");
                    System.out.printf("%s\n", new String(dat.getData(), dat.getOffset(), dat.getLength()));
                    */
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
