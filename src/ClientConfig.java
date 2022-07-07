public class ClientConfig {
    //variables to connect with WINSOMESERVER

    private String registryAddr = "localhost"; // Registry Address
    private int registryPort = 6789; //Registry port
    private String serverAddr = "localhost";    //server address
    private int serverPort = 9999; //Server port
    private String nameServer = "WINSOMESERVER"; //Service Name of server


    public ClientConfig() {
    }

    public String getRegistryAddr() {
        return registryAddr;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getNameServer() {
        return nameServer;
    }

}
