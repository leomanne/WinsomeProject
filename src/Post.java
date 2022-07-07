import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Post implements Serializable {
    //LOCKS used for concurrency
    @JsonIgnore private ReadWriteLock lock = new ReentrantReadWriteLock();
    @JsonIgnore private Lock readLock = lock.readLock();
    @JsonIgnore private Lock writeLock = lock.writeLock();

    //Local variable of the post
    private String author;
    private String title;
    private int id;     //id measured by the global value <id>
    private String date; //time of the creation of the comment
    private String content;
    private HashSet<String> upVotes; //saving name for both upVotes and downVotes
    private HashSet<String> downVotes;
    private HashMap<String, ArrayList<String>> comments;//saving for all the user that comments all the comments
    //used in reward thread formula
    public int n_iteration = 0;

    /**
     *
     * @param owner name of the user who wrote the post
     * @param title title of the post
     * @param content content of the post
     * @param postId id of the post
     * @param n_iteration number of iteration from server when this post is created
     */
    public Post(String owner, String title, String content,int postId,int n_iteration){
        this.author = owner;
        this.id = postId;
        this.title = title;
        this.content = content;
        SimpleDateFormat tmp = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        this.date =  tmp.format(new Date());
        this.comments = new HashMap<>();
        this.upVotes = new HashSet<>();
        this.downVotes = new HashSet<>();
        this.n_iteration = n_iteration;
    }
    public Post(){
    }

    /**
     * @return n_iteration
     */
    public int getN_iteration(){
        readLock.lock();
        int n = n_iteration;
        readLock.unlock();
        return n;
    }

    /**
     *
     * @param username name of the user
     * @return
     */
    public String addUpVote(String username){
        //POST CONTAINING ALREADY AN UPVOTE
        readLock.lock();
        if(this.upVotes.contains(username)||this.downVotes.contains(username)){
            return "< User already voted";
        }
        readLock.unlock();
        writeLock.lock();
        this.upVotes.add(username);
        writeLock.unlock();
        return "< Rate of ["+username+"] on postID ["+this.id+"] is successful";//id Post unchangeable
    }
    /**
     *
     * @param username name of the user
     * @return
     */
    public String addDownVote(String username){
        //POST CONTAINING ALREADY AN UPVOTE
        readLock.lock();
        if(this.upVotes.contains(username)||this.downVotes.contains(username)){
            return "< User already voted";
        }
        readLock.unlock();
        writeLock.lock();
        this.downVotes.add(username);
        writeLock.unlock();
        return "< Rate of ["+username+"] on post id["+this.id+"] is successful";
    }

    /**
     *
     * @return the title of this post
     */
    public String getTitle() {
        String response ;
        readLock.lock();
        response = title;
        readLock.unlock();
        return response;
    }

    /**
     *
     * @return the content of this post
     */
    public String getContent() {
        String response ;
        readLock.lock();
        response = content;
        readLock.unlock();
        return response;
    }

    /**
     *
     * @return the ID of this post
     */
    public int getID() {
        int response;
        readLock.lock();
        response = id;
        readLock.unlock();
        return response;
    }

    /**
     *
     * @return the name of the author of this post
     */
    public String getAuthor() {
        String response;
        readLock.lock();
        response = author;
        readLock.unlock();
        return response;
    }

    /**
     *
     * @return the size of the upVotes
     */
    public int getUpVotesTotal() {
        readLock.lock();
        int result = this.upVotes.size();
        readLock.unlock();
        return result;
    }

    /**
     *
     * @return the size of downVotes
     */
    public int  getDownVotesTotal() {
        readLock.lock();
        int result = this.downVotes.size();
        readLock.unlock();
        return result;
    }

    /**
     *
     * @param username name of a User
     * @param comment  comment to add
     * @return a boolean that represent the result of add operation
     */
    public boolean addComment(String username, String comment) {
        boolean response;
        this.writeLock.lock();
        comments.putIfAbsent(username,new ArrayList<>());//in case its first user's comment
        response = comments.get(username).add(comment);
        this.writeLock.unlock();
        return response;
    }

    /**
     *
     * @return a string containing names and comments of this post
     */
    public String getComments() {
        StringBuilder response = new StringBuilder();

        this.readLock.lock();
        for (Map.Entry<String,ArrayList<String>> t :comments.entrySet()) {
            String nome = t.getKey();
            ArrayList<String> list = t.getValue();
            response.append("< Nome: ").append(nome).append("\t| ");
            for (String s: list) {
                response.append(s).append(" | ");
            }
            response.append("\n");
        }
        this.readLock.unlock();
        return response.toString();
    }
}
