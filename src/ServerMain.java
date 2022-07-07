
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class ServerMain {

    public static void main(String[] args) {
        String path = "";
        if(args.length>0) {
            path = args[0];
        }
        ServerConfig config;
        if(args.length>0){
            config = readConfigFile(path);
        }else {
            config = readConfigFile("serverConfig.json");
        }
        Server server = new Server(config);
        server.start();

    }

    private static void WriteServerConfig(ServerConfig test, String path) {
        //for test only. this function writes an example for configuration file
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(path);
        System.out.println("Writing JSON object to file");
        System.out.println("-----------------------");
        try {
            file.createNewFile();
            mapper.writeValue(file, test);
            System.out.println("Writing JSON object to string");
            System.out.println(mapper.writeValueAsString(test));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static ServerConfig readConfigFile(String s) {
        ServerConfig c = new ServerConfig();
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(s);
        if (file.exists()) { //if file exists we read the info
            try {
                c = mapper.readValue(file, ServerConfig.class);
                System.out.println("Deserialized ServerConfig File from JSON");
                System.out.println("Server port = [" + c.getTcpServerPort() + "]\n" + "Registry port = " + "[" + c.getRegistryPort() + "]");
                System.out.println("ServerMc Address/Port = [" + c.getMCAddress() + "/" + c.getMCPort() + "]");
                //printing other info would be unnecessary


            } catch (FileNotFoundException | JsonMappingException | JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Config file Not Found! fetching default values");
            System.out.println("Server port = [" + c.getTcpServerPort() + "]\n" + "Registry port = " + "[" + c.getRegistryPort() + "]");
            System.out.println("ServerMc Address/Port = [" + c.getMCAddress() + "/" + c.getMCPort() + "]");
        }

        return c;
    }

}
