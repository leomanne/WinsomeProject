import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
public class ClientMain {

    public static void main(String[] args) {

        String path = "";
        Client client;
        ClientConfig config;
        if(args.length>0){//if path is passed then try to read file
            path = args[0];
            config = ReadConfig(path);
        }else { //else read standard path
            config = ReadConfig("clientConfig.json");
        }
        client = new Client(config); //create Client with configuration

        //action to take when client is interrupted (sigint)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if(client.client!=null) {
                    if (client.client.isConnected()) {
                        if (client.nameUser != null) {
                            client.sendMessage("-logout " + client.nameUser);
                            System.out.println(client.readMessage());
                        }
                        client.sendMessage("-exit");
                        client.chiusuraClient(client.client);
                        client.exit = true;
                    }
                }
            }));

        client.start();//start client
        //if used -exit request exit from application
        System.exit(0);
    }

    /**
     *
     * @param test Clientconfiguration class
     * @param path string containing the path of the Client configuration
     */
    //used for testing
    private static void WriteClientConfig(ClientConfig test, String path) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(path);
       System.out.println("Writing JSON object to file");
        System.out.println("-----------------------");
        try {
            file.createNewFile();
            mapper.writeValue(file, test);
            System.out.println("Writing JSON object to string");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     *
     * @param s path of a client configuration file
     * @return a ClientConfig with @s as path if exists else
     */
    private static ClientConfig ReadConfig(String s) {
        ClientConfig c = new ClientConfig();
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(s);
        if (file.exists()) { //if file exists we read the info
            try {
                c = mapper.readValue(file, ClientConfig.class);
                System.out.println("Deserialized ConfigClient File from JSON");
                System.out.println("Server Address/port = [" + c.getServerAddr() + "/" + c.getServerPort() + "]\n" + "Registry Address/port = [" + c.getRegistryAddr() + "/" + c.getRegistryPort() + "]");
                System.out.println("---------------Welcome in " + c.getNameServer()+"---------------");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else { // we use standard info already on @c
            System.out.println("Config file Not Found! fetching default values");
            System.out.println("Server Address/port = [" + c.getServerAddr() + "/" + c.getServerPort() + "]\n" + "Registry Address/port = [" + c.getRegistryAddr() + "/" + c.getRegistryPort() + "]");
            System.out.println("---------------Welcome in " + c.getNameServer()+"---------------");
        }

        return c;
    }

}
