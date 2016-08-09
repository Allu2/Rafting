import org.json.simple.JSONObject;
import raft.RaftResult;
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
        clearVotes(term);
        clearAppendedResponses(term);
    }

    public static void setTerm(int term){
        cTerm = term;
    }

    private static void clearAppendedResponses(int term) {

    }

    private static boolean clearVotes(int term) {
        if (cTerm == term){
            cVotes.clear();
            return true;
        }
        return false;
    }

    private static JSONObject getVotes(int term){
        if(cTerm == term) {return cVotes;}
        return null;
    }

    public static boolean setVote(String uid, RaftResult response, int candidateTerm) {
        if(cTerm == response.getTerm()){
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

    public static boolean cleatAppendResponses(int term){
        if(cTerm == term){
            cAppendResponses.clear();
            return true;
        }
        return false;
    }

    public static boolean setAppendResponse(String uid, RaftResult response, int leaderTerm) {
        if(cTerm == response.getTerm()){
            cAppendResponses.put(uid, response.isSuccessful());
            return true;
        }
        return false;
    }
}
