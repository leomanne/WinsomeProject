import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

public interface ClientInterface extends Remote { //extension used to declare these methods accessible from remote
    /**
     * @param users Hashset of some user's username
     * @throws RemoteException
     */
    void update(HashSet<String> users) throws RemoteException;

    /**
     * @return a string containing the username of this client
     * @throws RemoteException
     */
    String getName() throws RemoteException;

    /**
     *
     * @return return a set of strings containing usernames of the user who follow this user. used for list follower function
     * @throws RemoteException
     */
    Set<String> getFollowers() throws RemoteException;

}