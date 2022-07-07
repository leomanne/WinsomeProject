import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


//Data Structure to contain data about the server such as : users , posts , etc...
//and to contain and offer basic functionality functions to the server
public class  DTStructure {


    //Server config
    private static ServerConfig sc;

    //File for backup (default setting)
    private static final File userBackUp = new File("usersBackUp.json");
    private static final File postBackUp = new File("postsBackUp.json");
    private static final File  rewardBackUp = new File("rewardBackUp.json");


    //maps to save data about user,post, comment and tags
    private static ConcurrentHashMap<String, User> users_logged = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, User> users  = new ConcurrentHashMap<>();

    //this post is used to store the global information of all post : id as an Integer and the Post(with all the information inside)
    private static ConcurrentHashMap<Integer, Post> post = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, NewsOnPost> modifies = new ConcurrentHashMap<>();


    //Global Variables
    private static double btc = 0; //modified by thread rewardhandler
    private static int postID = 0; //used to keep tracks of the post , assigning the right id (IMPORTANT TO USE SYNCHRONIZED METHODS)
    private static volatile int  rewardIteration = 1; //used to keep track of the number of iteration already done

    //RMI object created and registered on registry in SERVER
    //used to notify to clients if a user has been unfollowing him or following him
    private static ServerInterfaceImpl serverInterface = null;

    //constructor method
    public DTStructure(ServerConfig sc, ServerInterfaceImpl serverImpl) throws RemoteException {
        DTStructure.serverInterface = serverImpl;
        DTStructure.sc = sc;
    }

    /**
     *
     * @return a @users_logged
     */
    public static ConcurrentHashMap<String, User> getUserLogged() {
        return users_logged;
    }

    /**
     *
     * @param username string with username of a user to register
     * @param password String of a password
     * @param tags     String of a number of tags (max 5)
     * @return a integer value. 0 if username is already taken 1 else
     */
    public static int addusers(String username, String password, String tags)  {
        if (UserNameExists(username)) return 0;
        else {
            users.putIfAbsent(username, new User(username, password, tags));
            return 1;
        }
    }

    /**
     *
     * @param username A string containing a username
     * @return true if username is already on @users, false otherwise
     */
    public static boolean UserNameExists(String username) {
        if (users==null) return false;
        for (String s : users.keySet()) {
            if (s.equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * MAIN FUNCTION to handle all the requests
     * @param msg A string containing the request
     * @return a string containing server Response
     */
    public static String handler(String msg) {
        String[] args = msg.split(" ", 0);
        String args1 = args[0];
        if(args.length>1) {
            args1 = args[0] + " " + args[1];
        }
        try {
            /**
             * Login function -login <username> <pwd>
             *
             */
            if (args1.contains("-login")) {
                return LoginUser(args);
            }
            /**
             * follow Function -follow <username> + username from client
             */
            else if (args1.contains("-follow")) {
                return Follow(args);
            }
            /**
             * list following function - list following + username from client
             */
            else if (args1.contains("-list following")){
                String username = args[2];
                return DTStructure.listFollowing(username);
            }
            /**
             * list users function -list users + username from client
             */
            else if (args1.contains("-list users")){
                String username = args[2]; //get name from -list users username <--- attached by client
                return DTStructure.listUsers(username);
            }
            /**
             * unfollow function -unfollow <username> + username from client
             */
            else if(args1.contains("-unfollow")){
                return Unfollow(args);
            }
            /**
             * view blog function -blog + username from client
             */
            else if (args1.contains("-blog")){
                String username = args[1]; //get name from -list users username <--- attached by client
                User user = DTStructure.findUser(username);
                return DTStructure.viewBlog(user);
            }
            /**
             * logout function -logout + username from client
             */
            else if (args1.contains("-logout")) {
                String username = args[1]; //-logout username
                return DTStructure.removeLoggedUser(username);
            }
            /**
             * Post function -post "title" "content" + username from client
             */
            else if (args1.contains("-post")) {

                String[] args2 = msg.trim().split("\"", 0);
                if(args2.length!=5)return "< Input Error";
                User user = DTStructure.findUser(args2[4].trim()); //-logout username
                if(user==null)return "< Wrong input";
                String title = args2[1].trim();
                if(!args2[2].equals(" "))return "< Wrong input";
                String content = args2[3].trim();
                return DTStructure.CreatePost(title,content,user);
            }
            /**
             * Show post Function -show post <id>
             */
            else if (args1.contains("-show post")){

                int id = Integer.parseInt(args[2]);
                return DTStructure.ShowPost(id);

            }
            /**
             * Delete post Function -delete <id>
             */
            else if (args1.contains("-delete")) {
                return DTStructure.deletePost(args);
            }
            /**
             * Rate post Function -rate <id> <+-1>
             */
            else if (args1.contains("/rate")) {
                return DTStructure.ratePost(args);
            }
            /**
             * Show feed Function -show feed + username from client
             */
            else if (args1.contains("-show feed")) {
                User user = DTStructure.findUser(args[2]);
                if(user==null)return "< User not found";
                return DTStructure.ShowFeed(user);
            }
            /**
             * Wallet btc function -wallet btc +(username)
             */
            else if (args1.contains("-wallet btc")) {
                User user = DTStructure.findUser(args[2]);
                if(user==null)return "< User not found";
                return DTStructure.walletBTC(user);
            }
            /**
             * Wallet function -wallet + username from client
             */
            else if (args1.contains("-wallet ")) {
                User user = DTStructure.findUser(args[1]);
                if(user==null)return "< User not found";
                return DTStructure.wallet(user);
            }
            /**
             * Comment post Function -comment <idPost> "Comment" (+ username from client)
             */
            else if (args1.contains("-comment ")) {
                String[] tmp = msg.split("\"",0);
                User user = DTStructure.findUser(tmp[2].trim());
                String comment = tmp[1].trim();
                int postID = Integer.parseInt(args[1].trim());
                //return tmp[2] +"|"+tmp [1] +"|"+args[1];
                if(user==null)return "< User not found";
                Post p = post.get(postID);
                if(p == null)return "< Post not found";
                return DTStructure.commentPost(user,p,comment);
            }
            /**
             *  Rewin Function -rewin <postID> + username from client
             */
            else if (args1.contains("-rewin")) {
                String username = args[2]; //-logout username
                User user = DTStructure.findUser(username);
                if (user == null) return "< User not found";
                Post p = post.get(Integer.parseInt(args[1]));
                if (p == null) return "< Post not found";
                return DTStructure.rewinPost(user, p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg+" is incorrect"; //if req is not on these options send back the request msg
    }

    /**
     *
     * @param user represent a User
     * @return a string with the total of a user wallet + the total in BTC currency (handled by reward thread)
     */
    private static String walletBTC(User user) {
        String response;
        response = "< Total : "+user.getTotalWallet()+"\n< Total in BTC = "+user.getTotalWallet()*DTStructure.btc;
        return response;
    }

    /**
     * @param user represent a user
     * @return a string with the total of a user wallet + all his movement
     */
    private static String wallet(User user) {
        String response;
        response = "< Total : "+user.getTotalWallet()+"\n< Movement: \n";
        response = response+user.getInfoWallet();
        return response;
    }

    /**
     *
     * @param user represent a User
     * @param p represent a Post
     * @effect adds a post on @user's blog
     * @requires @user must follow @p author
     * @return a response message
     */
    private static String rewinPost(User user, Post p) {
        if(user.getNome().equals(p.getAuthor()))return "< Cannot rewin your own post";
        if(user.addPost(p.getID())){
            return "< Rewin post successful";
        }else return "< Rewin post unsuccessful";
    }

    /**
     *
     * @param user represent a User
     * @param p represent a Post
     * @param comment represent a comment to add on Post @p
     * @effect adds a comment on a post
     * @requires @user must follow p author
     * @return a response message
     */
    private static String commentPost(User user, Post p, String comment) {
        if(comment==null)return "< Comment not read";
        if (!DTStructure.IsInUserFeed(user,p.getID()))return "< Post not in user feed";
        if(p.addComment(user.getNome(),comment)) {
            if(modifies.containsKey(p.getID())){
                NewsOnPost tmp = modifies.get(p.getID());//get the modifies on the post
                tmp.AddNews(user.getNome(),0);
            }else {
                modifies.putIfAbsent(p.getID(),new NewsOnPost(user.getNome(),0));
            }

            return "< Comment added";
        }return "< Comment not added";
    }

    /**
     *
     * @param user The user who request to see his feed
     * @return a String containing all the (ID - AUTHOR - TITLE) post in the user's feed
     */
    private static String ShowFeed(User user) {
        StringBuilder response = new StringBuilder();
        //for each user who is followed by @user are saved the posts
        for (String t: user.getFollowing()) {
            User tmp = DTStructure.findUser(t);
            if(tmp!=null) {
                for (int postID : tmp.getBlog()) {
                    Post p = post.get(postID);
                    if(p!=null){
                        response.append("< ID:\t").append(p.getID()).append("\n< Author:\t").append(p.getAuthor()).append("\n< Title:\t").append(p.getTitle()).append("\n");
                        response.append("------------------------------------------\n");
                    }
                }
            }
        }
        return response.toString();
    }

    /**
     *
     * @param args A vector of strings. -> @msg.split
     * @return A string containing the result of the operation
     * @effect adds a vote on a certain @post
      */
    private static String ratePost(String[] args) {//-rate <id> <+-1> username
        User user = DTStructure.findUser(args[3]);
        if(user==null)return "< User not found";
        int postID = Integer.parseInt(args[1]);
        Post p = post.get(postID);
        if(p == null)return "< Post does not exists";
        int valueRate;
        if(args[2].contains("+1")){
            valueRate=1;
        }else if(args[2].contains("/1")){
            valueRate=-1;
        }else{
            return "< Value rate not valid! accepted +1/-1";
        }
        //rate post only if it is in user's feed
        if(DTStructure.IsInUserFeed(user,postID)){
            if(valueRate==1){
                String s = p.addUpVote(user.getNome());
                if(s.contains("is successful")){//if rate response is positive then adds the info on the recent mofified
                    if(modifies.containsKey(p.getID())){
                        NewsOnPost tmp = modifies.get(p.getID());//get the modifies on the post
                        tmp.AddNews(user.getNome(),1);
                    }else {
                        modifies.putIfAbsent(p.getID(),new NewsOnPost(user.getNome(),1));
                    }
                }
                return s;

            }else {
                String s = p.addDownVote(user.getNome());
                if(s.contains("is successful")){//same
                    if(modifies.containsKey(p.getID())){
                        NewsOnPost tmp = modifies.get(p.getID());//get the modifies on the post
                        tmp.AddNews(user.getNome(),2);
                    }else {
                        modifies.putIfAbsent(p.getID(),new NewsOnPost(user.getNome(),2));
                    }
                }
                return s;
            }
        }else {return "< Post "+postID+" is not in "+user.getNome()+" feed";}

    }

    /**
     *
     * @param user represent a User
     * @param postID represent an ID for a Post
     * @return a boolean. true if a post is in @user's feed , false otherwise
     */
    private static boolean IsInUserFeed(User user ,int postID) {
        HashSet<String> tmp = user.getFollowing();
        for(String t:tmp){
            User u = DTStructure.findUser(t);
            if(u.hasPost(postID)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param args A vector of string, @msg.split  -delete <id>
     * @effects Remove a post from @DTstructure.post (Global storing) and from the user's post list
     * @return A string containing the result of the operation to send to the client
     */
    private static String deletePost(String[] args) {
        String response ;
        String username = args[2].trim();
        User user = DTStructure.findUser(username);
        int postID = Integer.parseInt(args[1]);
        if(user==null)return "< User not found";

        Post p = post.get(postID);
        if(p==null)return "< Post does not exist";
        if(p.getAuthor().equals(username)){ //REMOVE only if user is the owner
            if(user.removePost(postID)){
                post.remove(postID);
                response = "< Post removed correctly";
            }else{return "< Post not removed";}

        }else {return "< "+username+" is NOT the author of post ["+postID+"]";}
        return response;
    }

    /**
     *
     * @param id represent a Post's ID
     * @return a string with informations about the post with @id as his ID
     */
    private static String ShowPost(int id) {
        String response ;
        Post p = post.get(id);
        if(p==null){return "< Wrong ID. Post does not exists";}
        response =
                "< Title:\t"+p.getTitle()+"\n"
                +"< Content:\t"+p.getContent() +"\n"
                +"< Vote:\t"+p.getUpVotesTotal()+" Positive ,"+p.getDownVotesTotal()+" Negative\n"
                +"< Comment:\n";
        response = response + p.getComments();
        return response;

    }

    /**
     *
     * @param title a string
     * @param content a string
     * @param user represent a certain User
     * @effect create a post on @user's blog
     * @return message response if it has been created a post or not.
     */
    private static String CreatePost(String title,String content, User user) {
        String response;
        int postId = DTStructure.getIDPost();
        post.putIfAbsent(postId,new Post(user.getNome(),title,content,postId,DTStructure.getIteration()));
        boolean guard = user.addPost(postId);
        if(guard){
            response = "< Post added successfully in user";
        }else {
            response =  "< Post not Added";
        }
        return response;

    }

    /**
     * @return rewardIteration
     */
    private synchronized static int getIteration() {
        return rewardIteration;
    }

    /**
     *  @effect increment rewardiIteration
     */
    private synchronized static void incrementIteration() {
        rewardIteration++;
    }


    /**
     *
     * @param user the author
     * @return  A string containing (idPost, author e title) about all the post that @user created
     */
    private static String viewBlog(User user) {
        if (user==null)return "< User not found";
        StringBuilder response = new StringBuilder();
        HashSet<Integer> idPostsUser = user.getBlog(); // return all id from user's blog
        if (idPostsUser==null){
            return  "< No post were found for User :"+ user.getNome();
        }else { //if some are found return the info
            for (Integer t : idPostsUser) {
                Post p = post.get(t);
                if (p != null) {
                    response.append("-----------------------------------------------\n").append("< ID:\t").append(p.getID()).append("\n").append("< Author:\t").append(p.getAuthor()).append("\n").append("< Title:\t").append(p.getTitle()).append("\n");

                }
            }
            if(response.toString().equals("")){
                response = new StringBuilder("< Posts not found for User");
            }
            return response.toString();
        }
    }

    /**
     *
     * @param args -> the message sent by client divided
     * @return a string with a response of the operation
     * @effects call unfollowUser function after taking the 2 users needed
     */
    private static String Unfollow(String[] args) {
        User user1 = DTStructure.findUser(args[2]);//get name from -list users username <--- attached by client
        User user2 = DTStructure.findUser(args[1]);//same here
        if (user1 == null) return "< Strange error"; //it should never activate as args2 is the name of the user who activate the method
        if (user2 == null) return "< User: " + args[1] + " Not found";
        return DTStructure.unfollowUser(user1,user2);//user who called the function , user to unfollow
    }

    /**
     *
     * @param args -> the message sent by client divided
     * @return a string with a response from the operation
     * @effects follow a user
     */
    private static String Follow(String[] args) {
        //Adding to user following this username
        if (args.length > 3) {
            return "Too many arguments...";
        } else if (args.length < 2) {
            return "It needs user name too...";
        } else {//add both to following list and followed list of user1 and user2
            //System.out.println("if " + args[2] + " .equals " + args[1]);
            return DTStructure.followUsers(args);
        }
    }

    /**
     *
     * @param args -> the message sent by client divided
     * @return a string with the response from the operation
     * @effects adds to users_logged the username <==> username ! already in user_logged
     */
    private static String LoginUser(String[] args) {
        //if (args.length>3) return "Too many arguments"; control already done in client
        try {
            int guard = DTStructure.login(args[1], args[2]);
            if (guard==0) {
                return "Error in login ,Not existing user";
            } else if(guard==1){
                users_logged.putIfAbsent(args[1], DTStructure.findUser(args[1]));
                return "success login " + args[1] + " " + DTStructure.sc.getMCAddress() + " " + DTStructure.sc.getMCPort();
                //the name of the user
            }else if(guard == 2){
                return "Error in login ,Wrong password";
            }else {
                return "User already logged in!";
            }
        } catch (RemoteException e) {
            return "Remote exception";
        }

    }

    /**
     *
     * @param args -> the message sent by client divided
     * @return a string with the result of the operation
     * @effects adds in following and followers of the users called by this method the usernames
     * user1 == user who called the method
     * user2 == user who needs to be followed by user1
     * adding in user1->following{user2}
     * adding in user2->followers{user1}
     */
    private static String followUsers(String[] args) {
        if (args[2].trim().equals(args[1].trim())) return "You can't follow yourself";
        //taking the user first
        User user1 = DTStructure.findUser(args[2]);
        if (user1 == null)
            return "Strange error"; //it should never activate as args2 is the name of the user who activate the method
        User user2 = DTStructure.findUser(args[1]);
        if (user2 == null) return "User: " + args[1] + " Not found";
        //Adding in user1 -> Following and user2->followed

        if(!user1.addFollowing(args[1]))return "< Error adding follower"; //the user who called the method
        if(!user2.addFollower(args[2]))return "< Error adding follower";  // the User who now user1 should follow
        //SEND NOTIFICATION TO the server interface witch will notify the ClientInterface
        DTStructure.serverInterface.sendNotification(args[1]);
        return "< "+user2.getNome()+" followed";
    }

    /**
     *
     * @param user1 the user who called the function
     * @param user2 the user who needs to be unfollowed by user1
     * @return a string containing the result of the operation
     *@effects remove in following and followers of the users called by this method the usernames
     * user1 == user who called the method
     * user2 == user who needs to be followed by user1
     * removing from user1->following{user2}
     * removing from user2->followers{user1}
     */
    private static String unfollowUser(User user1,User user2 ) {
        if(!user1.hasFollowingUser(user2.getNome())){
            return "You do not follow "+user2.getNome();
        }
        if(user1.removeFollowing(user2.getNome())){
            if (user2.removeFollower(user1.getNome())){
                //SEND NOTIFICATION TO the server witch will notify the ClientInterface
                DTStructure.serverInterface.sendNotification(user2.getNome());
                return user2.getNome()+" unfollowed";
            }
        }
        return "User not unfollowed correctly";
    }

    /**
     *
     * @param username a string containing the UNIQUE value of a User
     * @return response of the operation to send to client
     * @effects adds in String resp all the users username and tags from username->following list
     */
    private static String listFollowing(String username) {

        StringBuilder resp = new StringBuilder();
        User user = DTStructure.findUser(username);
        for (String t : user.getFollowing()) {
            User tmp = DTStructure.findUser(t);
            if (tmp != null) {
                resp = new StringBuilder(resp + "< " + t + "\t| " + tmp.getTags() + " \n");
            }
        }
        return resp.toString();
    }

    /**
     * @param username a string containing the UNIQUE value of a User
     * @return a String containing the users with at least one tag in common
     */
    private static String listUsers(String username) {

        StringBuilder resp= new StringBuilder();
        User user = DTStructure.findUser(username);
        for (Map.Entry<String, User> entry : users.entrySet()) {
            String key = entry.getKey();
            User tmpUser = entry.getValue();
            if(!user.getNome().equals(tmpUser.getNome())&&DTStructure.hasTagInCommon(user,tmpUser)) {
                String tagInCommon = DTStructure.getTagInCommon(user, tmpUser);
                resp.append("< ").append(key).append("\t| ").append(tagInCommon).append(" \n");

            }

        }
        return resp.toString();
    }

    /**
     *
     * @param user1  the user who called the function
     * @param user2  a different user from user1
     * @return a boolean that represent if the 2 users have tags in common
     */
    public static boolean hasTagInCommon(User user1, User user2) {
        String[] tagsuser1 = user1.getTags().split(" ",0);
        String[] tagsuser2 = user2.getTags().split(" ",0);
        for (String t1 : tagsuser1){
            for (String t2: tagsuser2) {
                if(t1.equals(t2)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param user1  the user who called the function
     * @param user2  a different user from user1
     * @return a string containing all the tag in common between the users
     */
    public static String getTagInCommon(User user1, User user2) {
        StringBuilder tags = new StringBuilder();
        String[] tagsuser1 = user1.getTags().split(" ",0);
        String[] tagsuser2 = user2.getTags().split(" ",0);
        for (String t1 : tagsuser1){
            for (String t2: tagsuser2) {
                if(t1.equals(t2)){
                    tags.append(t1).append(" ");
                }
            }
        }
        return tags.toString();
    }

    /**
     * @param username a string containing the UNIQUE value of a User
     * @return a String parameter that indicates whether the op has resulted in error or not
     * @effects remove from user_logged the username and the value associated
     */
    private static String removeLoggedUser(String username) {
        if (DTStructure.users_logged.remove(username)!=null){
            return "ok logout";
        }//removing entry
        return "error logout";
    }

    /**
     * @param username a string containing the UNIQUE value of a User
     * @return the User that has @param username (usernames are UNIQUE)
     */
    private static User findUser(String username) {
        return users.get(username);
    }

    /**
     *
     * @param username a string containing the UNIQUE value of a User
     * @param pwd a string containing the password of a user
     * @return an integer value that represent the result of the operation(0 not found user,1 ok ,2 not ok pwd ,3 is already logged in)
     * @throw RemoteException
     */
    private static int login(String username, String pwd) throws RemoteException {
        User u  = users.get(username);
        if(u==null) {
            return 0;
        }
        if(DTStructure.isAlreadyLoggedIn(username)){
            return 3;
        }
        if(u.getPwd().equals(pwd))return 1;

        return 2;
    }

    /**
     * @param username a string containing the UNIQUE value of a User
     * @return a boolean value , true if the users_logged already has a user with @param username value
     * false otherwise
     */

    private static boolean isAlreadyLoggedIn(String username) {
        return DTStructure.users_logged.containsKey(username);
    }

    /**
     *
     * @param username a string containing the UNIQUE value of a User
     * @return HashSet<User> with the @param username's follower
     * @throw RemoteException
     */

    public static HashSet<User> getFollowers(String username) throws RemoteException {
        if (!users.containsKey(username))
            return new HashSet<>(); //return empty hashset
        HashSet<User> result = new HashSet<>();
        //get all the user from user's follower list
        for (String u : users.get(username).getFollowers()) {
            if (users.containsKey(u)) {
                User user = new User(users.get(u).getNome(), users.get(u).getPwd(), users.get(u).getTags());
                result.add(user);
            }
        }
        return result;
    }

    /**
     * @effect calculate the rewards for all user looking at upvote down vote , comments and posts
     */

    public static void reward() {
        //first increment iteration
        DTStructure.incrementIteration();
        //create a hashmap with all the elements from @modifies
        ConcurrentHashMap<Integer, NewsOnPost> maptemp = new ConcurrentHashMap<>(modifies);
        //for each entry take id and the news on the post with thta id
        for (Map.Entry<Integer,NewsOnPost> entry : maptemp.entrySet()) {
            int id = entry.getKey();
            NewsOnPost tmp = maptemp.get(id);
            Post p = post.get(id);
            // check if the post still exists
            if (p != null) {
                // get the number of upVotes, downVotes, and the number of comments for each user
                int numUpVotes =tmp.n_upVotes;
                int downVotes = tmp.n_downVotes;
                HashMap<String, Integer> map = tmp.n_comments; //string is the name of a user and integer is the number of comments


                // now apply the formula to each user and calculate the sum

                double resultComments = 0;
                for (Map.Entry<String, Integer> entryhashmap : map.entrySet()) {
                    int cp = entryhashmap.getValue();
                    resultComments += 2 / (1 + Math.pow(Math.E, -(cp - 1)));
                }
                double reward = (Math.log(Math.max(numUpVotes - downVotes, 0) + 1) + Math.log(resultComments + 1)) /(getIteration() - p.getN_iteration());

                // author reward
                if (users.containsKey(p.getAuthor())) {
                    User author = DTStructure.findUser(p.getAuthor());
                    author.addRewardAuthor(reward);
                }

                // normal reward

                // get all the users who interacted with the post
                HashSet<String> usersInteract = tmp.usersInteract;
                for (String t : usersInteract) {
                    User user = DTStructure.findUser(t);
                    user.addNormalReward(reward / usersInteract.size());
                }


            }
            //remove from hashmap the id of the current post
            modifies.remove(id);
        }

    }

    /**
     *
     * @effects create backUp of the Users,Posts and the integer rewardIteration
     */

    public static void BackUp() {
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE); //turn off everything
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            mapper.writeValue(userBackUp, users);
            mapper.writeValue(postBackUp, post);;
            mapper.writeValue(rewardBackUp, rewardIteration);
        }
        catch (FileNotFoundException e){
            System.out.println("Could not locate backup file");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @effects Function started before -BackUp- to create the new files if not existent
     */

    public static void createFile() {
        try {
                userBackUp.createNewFile();

                postBackUp.createNewFile();

                rewardBackUp.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @effects increment and return the idPost (global variable)
     * @return int idPost
     */

    public synchronized static int getIDPost() {
        return postID++;
    }

    /**
     * @effects if all file exists then copy from these files the data to hashmaps user,posts and rewarditeration
     */

    public static void getBackUp() {
        try {
            if (!(userBackUp.exists() && postBackUp.exists() && rewardBackUp.exists())) {
                System.out.println("-------------------------------------------------------");
                System.out.println("Cannot restore internal status: One or more .json files missing");
                System.out.println("-------------------------------------------------------");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE); //turn off everything
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            //creating bufferedReader with usersBackUp as file to read
            BufferedReader backupReader = new BufferedReader(new FileReader(userBackUp));
            //get the values from the file
            users = mapper.readValue(backupReader, new TypeReference<ConcurrentHashMap<String, User>>() {
            });

            System.out.println("-------------------------------------------------------");
            System.out.println("Backup users successfully done");
            backupReader = new BufferedReader(new FileReader(postBackUp));
            post = mapper.readValue(backupReader, new TypeReference<ConcurrentHashMap<Integer, Post>>() {
            });

           backupReader = new BufferedReader(new FileReader(rewardBackUp));
           rewardIteration = mapper.readValue(backupReader,Integer.class);
            System.out.println("Backup posts successfully done");
            System.out.println("-------------------------------------------------------");

            DTStructure.postID = DTStructure.getMaxPostID()+1;//get Last ID used +1

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the last used integer ID
     */

    private static int getMaxPostID() {
        int max = 0;
        for (Map.Entry<Integer, Post> entry : DTStructure.post.entrySet()){
            int t = entry.getKey();
            if(max < t){ max = t;}
        }
        return max;
    }

    /**
     * @effect get the value of a double from @url
     */

    public static void BTCReward() throws MalformedURLException {
        try {
            //create the url for the value on site
            URL url = new URL("https://www.random.org/decimal-fractions/?num=1&dec=1&col=1&format=plain&rnd=new");
            //send an HTTP GET request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine = "";
                String response = "";
                while ((inputLine = reader.readLine()) != null) {
                    response = response+inputLine;
                }
                reader.close();
                btc = Double.parseDouble(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * private class used to store data about the recent votes (+1,-1) and comments about post
     */

    private static class NewsOnPost {
        private ArrayList<Integer> choice= new ArrayList<>();
        private int n_upVotes;
        private int n_downVotes;
        private HashMap<String,Integer> n_comments = new HashMap<>();
        private HashSet<String> usersInteract = new HashSet<>();
        private NewsOnPost(String username,int choice){
            usersInteract.add(username);
            this.n_upVotes = 0;
            this.n_downVotes = 0;
            this.choice.add(choice);// 0 comment , 1 upvote , 2 downVote
            if(choice==1){
                this.n_upVotes++;
            }else if(choice == 2){
                n_downVotes++;
            }else {
                if (n_comments.containsKey(username)){
                    int oldVal = n_comments.get(username);
                    n_comments.replace(username,oldVal+1);
                }else{
                    n_comments.putIfAbsent(username,1);
                }
            }
        }

        /**
         *
         * @param username  name of a user
         * @param choice    represents upvotes,downvotes and comments
         */

        public void AddNews(String username , int choice){
            this.choice.add(choice);
            if(choice==1){
                this.n_upVotes++;
            }else if(choice == 2){
                n_downVotes++;
            }else {
                if (n_comments.containsKey(username)){
                    int oldVal = n_comments.get(username);
                    n_comments.replace(username,oldVal+1);
                }else{
                    n_comments.putIfAbsent(username,1);
                }
            }
        }
    }
}
