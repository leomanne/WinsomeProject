import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerInterfaceImpl extends UnicastRemoteObject implements ServerInterface {
    //map to keep track of all the client connected
    private ConcurrentHashMap<ClientInterface,String > clients;

    public ServerInterfaceImpl() throws RemoteException {
        super();
        this.clients = new ConcurrentHashMap<>();
    }

    /**
     *
     * @param username name of a user
     * @param password password of a user
     * @param tags tags of a user
     * @return  the response of the operation
     * @throws RemoteException
     */
    @Override
    public String register(String username, String password, String tags) throws RemoteException {
        if (username == null || password == null || tags == null)
            throw new NullPointerException();
        if (DTStructure.UserNameExists(username))
            return "User name already taken";
        DTStructure.addusers(username,password,tags);//add the user with tags and pwd to DTstructure @users
        // debug
        return username+" registered";
    }

    //CALLBACK REGISTERING METHODS

    /**
     *
     * @param cli Client interface used to keep track of all the clients connected
     * @throws RemoteException
     */
    @Override
    public void registerForCallback(ClientInterface cli) throws RemoteException {
        if(cli==null) return;
        String username = cli.getName();
        this.clients.putIfAbsent(cli, username);//performed atomically
        this.sendNotification(username);
        System.out.println("success registered " + cli.getName());
    }

    /**
     *
     * @param username name of a User
     * @effects send to the interested CLient interface the strings containing names of users who followed/unfollowed the @username
     */
    public void sendNotification(String username) {
        //filter from the values that are the same as username
        ClientInterface cli = null; //saving the clientInterface of the UNIQUE user username
        for (Map.Entry<ClientInterface, String> s : this.clients.entrySet()) {
            //System.out.println(s.getValue());

            if (s.getValue().equals(username)) {
                cli = s.getKey();
            }
        }
        if(cli == null)return;
        try {
            HashSet<String> strings = new HashSet<>();
            for (User user : DTStructure.getFollowers(username)) {
                String nome = user.getNome();
                strings.add(nome);
            }
            cli.update(strings);
            } catch (RemoteException e) {
                System.out.println("Error sending a notification..");
                //e.printStackTrace();
            }
    }

    /**
     *
     * @param cli Client interface of the user who logged out
     * @throws RemoteException
     */
    @Override
    public void unregisterForCallback(ClientInterface cli) throws RemoteException {
        if(cli==null){
            throw new NullPointerException();
        }
        if(this.clients.remove(cli)==null) {
            System.out.println("Unsuccess unregistered");
            return;
        }
        System.out.println("success unregistered " + cli.getName());

    }
}
