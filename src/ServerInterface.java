import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    /**
     * Function used to register a user
     * @param username name of a user
     * @param password password of a user
     * @param tags tags of a user
     * @return  a string containing the response from server
     * @throws RemoteException
     */
    public String register(String username, String password, String tags) throws RemoteException;

    /**
     *
     * @param cli Client interface used to keep track of all the clients connected
     * @throws RemoteException
     */
    public void registerForCallback(ClientInterface cli) throws RemoteException;

    /**
     *
     * @param username name of a User
     * @effects send to the interested CLient interface the strings containing names of users who followed/unfollowed the @username
     */
    public void sendNotification(String username)throws RemoteException;

    /**
     *
     * @param cli Client interface of the user who logged out
     * @throws RemoteException
     */
    public void unregisterForCallback(ClientInterface cli) throws RemoteException;

}


