public class ServerConfig {
    private int TcpServerPort = 9999; //server port
    private int registryPort = 6789;  //registry port
    private String nameServer = "WINSOMESERVER";

    private long rewardTimeout = 5000; //timeout for thread reward
    private int BUFFER_DIMENSION = 1204;
    private long bkupTimeOut = 10000;

    private int MCPort = 8888; //multicast port
    private String MCAddress = "239.255.1.1";

    public ServerConfig() {
    }

    /**
     * @return port used for tcp
     */
    public int getTcpServerPort() {
        return TcpServerPort;
    }

    /**
     * @return port used for registry
     */
    public int getRegistryPort() {
        return registryPort;
    }

    /**
     * @return timeout used by thread reward
     */
    public long getRewardTimeout() {
        return rewardTimeout;
    }

    /**
     * @return timeout used by back up thread
     */
    public long getBkupTimeOut() {
        return bkupTimeOut;
    }

    /**
     * @return Multicast port used by reward thread
     */
    public int getMCPort() {
        return MCPort;
    }

    /**
     * @return Multicast address used by reward thread
     */
    public String getMCAddress() {
        return MCAddress;
    }

    /**
     * @return buffer dimension used by TCP thread
     */
    public int getBUFFER_DIMENSION() {
        return BUFFER_DIMENSION;
    }

    /**
     * @return name of the server
     */
    public String getNameServer() {
        return nameServer;
    }
}
