import org.json.simple.JSONObject;
import java.util.ArrayList;

/**
 * Created by allu on 9.8.2016.
 */
public class RaftResponses {

    private static int cTerm; // c for current
    private static JSONObject cAppendResponses;
    private static JSONObject cVotes;

    public static void init (int term){
        cTerm = term;
        cAppendResponses = new JSONObject();
        cVotes = new JSONObject();
        clearVotes(term);
        clearAppendResponses(term);
    }

    public static void setTerm(int term){
        cTerm = term;
    }


    public static boolean clearVotes(int term) {
        if (cTerm == term){
            cVotes.clear();
            return true;
        }
        return false;
    }

    public static JSONObject getVotes(int term){
        if(cTerm == term) {return cVotes;}
        return null;
    }

    public static boolean setVote(String uid, RaftResult response, int candidateTerm) {
        System.out.println("setVote in RaftResponse term of candidate is "+uid+" with term:"+response.getTerm()+ ", our term:"+cTerm );
        if(cTerm == response.getTerm()){
            System.out.println("Setting vote from "+uid);
            cVotes.put(uid, response.isSuccessful());
            return true;
        }
        return false;
    }

    public static JSONObject getAppendResponses(int term){
        if(cTerm == term){
            return cAppendResponses;
        }
        return null;
    }

    public static boolean clearAppendResponses(int term){
        if(cTerm == term){
            cAppendResponses.clear();
            return true;
        }
        return false;
    }

    public static boolean setAppendResponse(String uid, RaftResult response, int leaderTerm) {
        if(cTerm == response.getTerm()){
            JSONObject helper = new JSONObject();
            helper.put("success", response.isSuccessful());
            helper.put("term", response.getTerm());
            helper.put("logConsistent", response.isLogConsistent());
            cAppendResponses.put(uid, helper);
            return true;
        }
        return false;
    }
}
