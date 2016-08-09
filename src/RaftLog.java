import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by aleksi on 8.8.2016.
 *
 * Not the most sophisticated solution, but easy and fast to implement.
 *   {
 *      "1": {
 *          "term": value,
 *          "entry": value
 *      }
 *   }
 *
 *
 *
 * TODO: Make this and RaftConfig have shared codebase.
 *
 */
class RaftLog {
    public JSONParser parser = new JSONParser();
    public File log_file;



    public JSONObject RaftLog(String path) {
        try {
            log_file = new File(path);
            String log = FileReader.getFileContents(log_file);
            Object obj = parser.parse(log);
            JSONObject json = (JSONObject) obj;
            System.out.println(json.toJSONString());
            return json;

        } catch (ParseException | FileNotFoundException e) {
            System.out.println("Failed parsing or reading the log file.");
            e.printStackTrace();
            return null; // Got to love null;
        }
    }

    /*public static void main(String args[]){
        RaftLog ra = new RaftLog();
        ra.RaftLog();
    }*/
}