import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//Class to represent the information about a user
public class User implements Serializable {
    private String nome;
    private String pwd;
    private String tags;
    //names set of user who follow this user
    private HashSet<String> followers;
    //names set of user who are followed by this user
    private HashSet<String> following;
    //set to keep track of all the post created by user
    private HashSet<Integer> blog;
    //list of transaction of this user
    private ArrayList<Transaction> wallet;
    private double authorPercentage = 0.7; //70%
    private double normalPercentage = 0.3; //30%

    //Locks
    //IMPORTANT TO IGNORE the locks -fixing com.fasterxml.jackson Exception
    @JsonIgnore private ReadWriteLock lock = new ReentrantReadWriteLock();
    @JsonIgnore private Lock readLock = lock.readLock();
    @JsonIgnore private Lock writeLock = lock.writeLock();
    public User(String nome, String pwd, String tags)  {
        this.nome = nome;
        this.pwd = pwd;
        this.tags = tags;
        this.followers = new HashSet<>();
        this.following = new HashSet<>();
        this.blog = new HashSet<>();
        this.wallet = new ArrayList<>();

    }
    public User() {
    }

    /**
     *
     * @return the total of the user's wallet
     */
    public double getTotalWallet(){
        double max = 0;
        this.readLock.lock();
        for (Transaction t: wallet) {
            max = max + t.getValue();
        }
        this.readLock.unlock();
        return max;
    }

    /**
     *
     * @return name
     */
    public String getNome() {
        String tmp;
        this.readLock.lock();
        tmp = nome;
        this.readLock.unlock();
        return tmp;
    }

    /**
     *
     * @return password
     */
    public String getPwd() {
        String tmp;
        this.readLock.lock();
        tmp = pwd;
        this.readLock.unlock();
        return tmp;
    }

    /**
     *
     * @return a string with the tags
     */
    public String getTags() {
        String tmp;
        this.readLock.lock();
        tmp = tags;
        this.readLock.unlock();
        return tmp;
    }

    /**
     *
     * @return a set of followers(users who follow this user)
     */
    public HashSet<String> getFollowers() {
        HashSet<String> tmp;
        this.readLock.lock();
        tmp = followers;
        this.readLock.unlock();
        return tmp;
    }

    /**
     *
     * @return a set of following (users who are followed by this user)
     */
    public HashSet<String> getFollowing() {
        HashSet<String> tmp;
        this.readLock.lock();
        tmp = following;
        this.readLock.unlock();
        return tmp;
    }

    /**
     *
     * @param user name of a user
     * @effect adds a user to the following
     * @return the result of the operation
     */
    public boolean addFollowing(String user) {
        boolean result;
        this.writeLock.lock();
        result = this.following.add(user);
        this.writeLock.unlock();
        return result;
    }

    /**
     *
     * @param user name of a user
     * @effect adds a user to followers
     * @return a boolean as a result of the operation
     */
    public boolean addFollower(String user) {
        boolean result;
        this.writeLock.lock();
        result = this.followers.add(user);
        this.writeLock.unlock();
        return result;
    }

    /**
     *
     * @param name username
     * @effect remove a user from following
     * @return result of the operation
     */
    public boolean removeFollowing(String name) {
        boolean result;
        this.writeLock.lock();
        result = this.following.remove(name);
        this.writeLock.unlock();
        return result;
    }

    /**
     *
     * @param name username
     * @effect remove a user from follower
     * @return result of the operation
     */
    public boolean removeFollower(String name) {
        boolean result;
        this.writeLock.lock();
        result = this.followers.remove(name);
        this.writeLock.unlock();
        return result;
    }

    /**
     *
     * @param name username
     * @return true if this user has @name as one of the following
     */
    public boolean hasFollowingUser(String name) {
        boolean result=false;
        this.readLock.lock();
        if(this.following.contains(name))
            result=true;
        this.readLock.unlock();
        return result;
    }

    /**
     *
     * @return a set of all the post created by this user
     */
    public HashSet<Integer> getBlog() {
        HashSet<Integer> resp;
        this.readLock.lock();
        resp = blog;
        this.readLock.unlock();
        return resp;
    }

    /**
     *
     * @param id is the id of the post to add as this user as author
     * @return true if operation has been successful, false otherwise
     */
    public boolean addPost(int id) {
        boolean response;
        this.writeLock.lock();
        response = this.blog.add(id);
        this.writeLock.unlock();
        return response;
    }

    /**
     *
     * @param postID is the id of the post to remove as this user as author
     * @return true if operation has been successful, false otherwise
     */
    public boolean removePost(int postID) {
        boolean result;
        this.writeLock.lock();
        result = this.blog.remove(postID);
        this.writeLock.unlock();
        return result;
    }

    /**
     *
     * @param postID is the ID of the post to check
     * @return true if this user has the post on his blog, false otherwise
     */
    public boolean hasPost(int postID) {
        for (int t: blog) {
            if(t==postID){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return a string containing the info about the wallet
     */
    public String getInfoWallet() {
        StringBuilder response = new StringBuilder();
        this.readLock.lock();
        if(!wallet.isEmpty()){
            for (Transaction t:wallet) {
                response.append("< Amount: \t| ").append(t.getValue()).append("\n< Date: \t| ").append(t.getDate()).append("\n");
            }
        }else {
            response = new StringBuilder("< No element in wallet");
        }
        this.readLock.unlock();
        return response.toString();
    }


    /**
     *
     * @param reward is the reward to add on this user wallet
     */
    public void addRewardAuthor(double reward) {
        this.writeLock.lock();
        double res = reward*authorPercentage;
        SimpleDateFormat tmp = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        this.wallet.add(new Transaction((tmp.format(new Date())), res));
        this.writeLock.unlock();

    }

    /**
     *
     * @param reward is the reward to add on this user wallet
     */
    public void addNormalReward(double reward) {
        this.writeLock.lock();
        double res = reward * normalPercentage;
        SimpleDateFormat tmp = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        this.wallet.add(new Transaction((tmp.format(new Date())), res));
        this.writeLock.unlock();
    }
}
