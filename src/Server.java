import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

class Server {
    //server configs taken on start
    private ServerConfig serverConfig;
    private DTStructure dati;
    //RMI server interface implementation
    private ServerInterfaceImpl serverImpl;
    //number of current active connection
    private int n_activeConnection;

    public Server(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.n_activeConnection = 0;
    }

    public void start() {
        //RMI SET-UP
        try {
            serverImpl = new ServerInterfaceImpl();
            LocateRegistry.createRegistry(serverConfig.getRegistryPort());//create registry on port specified by server config
            //publication of stub server on registry
            LocateRegistry.getRegistry(serverConfig.getRegistryPort()).rebind(this.serverConfig.getNameServer(), serverImpl);//bind on name server
            System.out.println("REGISTRY SET IN "+serverConfig.getRegistryPort()+" "+serverConfig.getNameServer());
            //passing server config and server implementation
            dati = new DTStructure(this.serverConfig,serverImpl);

            //get backups before starting the threads
            DTStructure.getBackUp();

            //start reward thread with MCPort , MCAddress and timeout from server config
            RewardHandler rewardHandler = new RewardHandler(this.serverConfig.getMCPort(),this.serverConfig.getMCAddress(),this.serverConfig.getRewardTimeout());
            Thread threadReward = new Thread(rewardHandler);
            threadReward.start();
            //start thread to save data about user,post and comments
            BackUpHandler backUp = new BackUpHandler(this.serverConfig.getBkupTimeOut());
            Thread threadBkp = new Thread(backUp);
            threadBkp.start();

            //must establish tcp connection between server and client
            ServerSelector serverSelector = new ServerSelector(n_activeConnection, serverConfig, dati);
            Thread threadTCP = new Thread(serverSelector);
            threadTCP.start();

            //adds a thread to handle sigint
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if(serverSelector.closeThreadPool()){//close the threadpool
                        System.out.println("--closing server ok--");
                    }else {System.out.println("--closing server not ok--");}
                    threadTCP.interrupt();
                    threadTCP.join();//wait for thread tcp to finish
                    threadReward.interrupt();
                    threadReward.join();//wait for reward thread to finish
                    threadBkp.interrupt();
                    threadBkp.join();//wait for back up thread to finish
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }));

        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("-----Error in Creating/Locating Registry-----");
            System.exit(0);
        }
    }


}