import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInterfaceImpl extends RemoteServer implements ClientInterface {
    //variables to keep local information about a user who logged in
    private String username;//name of current user who logged in
    private Set<String> usersfollowers = ConcurrentHashMap.newKeySet(); //set of users who follow this user

    protected ClientInterfaceImpl(String username) throws RemoteException {
        this.username = username;
    }

    /**
     *
     * @param users Hashset of some user's username
     * @effect update the set of user's followers
     * @throws RemoteException
     */
    @Override
    public void update(HashSet<String> users) throws RemoteException {
        //this collect all users FROM @usersfollower that are not in @usersfollowers(gets all of the usernames who were not in @usersfollower)
        Set<String> tmpRm;
        Set<String> result = new HashSet<>();
        for (String usersfollower : this.usersfollowers) {
            if (!users.contains(usersfollower)) {
                result.add(usersfollower);
            }
        }

        tmpRm = result;
        //removing all of these users from usersfollower
        for (String t : tmpRm) {
            this.usersfollowers.remove(t);
        }
        //Saving all the users that are in users but not in usersfollowing (basically new followers)
        Set<String> set = new HashSet<>();
        for (String s : users) {
            if (!this.usersfollowers.contains(s)) {
                set.add(s);
            }
        }
        tmpRm = set;
        //Adds all the new followers to the set
        this.usersfollowers.addAll(tmpRm);
    }

    /**
     *
     * @return username
     * @throws RemoteException
     */
    @Override
    public String getName() throws RemoteException {
        return username;
    }

    /**
     *
     * @return user's followers
     * @throws RemoteException
     */
    @Override
    public Set<String> getFollowers() throws RemoteException {
        return this.usersfollowers;
    }
}
