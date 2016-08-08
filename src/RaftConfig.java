import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by aleksi on 8.8.2016.
 *
 *   {
 *      "server_name": name,
 *      "local_rmi_server_ip": ip,
 *      "local_rmi_server_port: 1099,
 *      "log_path": path,
 *      "servers": {
 *          "uid": location,
 *      }
 *   }
 *
 *
 *
 *
 *
 */
class RaftConfig {
    public JSONParser parser = new JSONParser();
    public File config_file = new File("./config.json");



    public JSONObject RaftConfig() {
        try {
            String conffi = FileReader.getFileContents(config_file);


            Object obj = parser.parse(conffi);
            JSONObject json = (JSONObject) obj;
            System.out.println(json.get("server_name"));
            return json;

        } catch (ParseException | FileNotFoundException e) {
            System.out.println("Failed parsing or reading config file.");
            e.printStackTrace();
            return null; // Got to love null;
        }
    }

/*    public static void main(String args[]){
        RaftConfig ra = new RaftConfig();
        ra.RaftConfig();
    }*/
}