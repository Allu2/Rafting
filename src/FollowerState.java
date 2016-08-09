import org.json.simple.JSONObject;
import raft.RaftResult;

import java.util.Random;
import java.util.Timer;

/**
 * Created by allu on 9.8.2016.
 */
public class FollowerState extends BaseState {
    private Timer myLeaderTimeoutTimer;
    private int Leader_timeout_timer_id = 1;

    public void go(){
        synchronized (thred_lock){
            System.out.println("Switched to followe mode.");
            Random rand = new Random();
            myLeaderTimeoutTimer = scheduleTimer(rand.nextInt(timeout_max-timeout_min)+timeout_min, this.Leader_timeout_timer_id);
        }
    }

    private void resetLeaderTimeoutTimer(){
        myLeaderTimeoutTimer.cancel();
        Random rand = new Random();
        myLeaderTimeoutTimer = scheduleTimer(rand.nextInt(timeout_max-timeout_min)+timeout_min, this.Leader_timeout_timer_id);
    }

    public RaftResult requestVote(int candidateTerm,
                                  String candidateUID,
                                  int lastLogIndex,
                                  int lastLogTerm){
        synchronized (thred_lock){
            RaftConfig config_object = new RaftConfig();
            JSONObject conf = config_object.RaftConfig();
            int term = (Integer) conf.get("last_term");
            String last_vote = (String) conf.get("last_vote");
            int last_index = (Integer) conf.get("last_index");
            RaftResultImp result = new RaftResultImp();
            if(candidateTerm >= term && last_vote.equals("-1") && lastLogIndex>=last_index){ // Using magic value to get stuff done :P
                System.out.println("Server "+ conf.get("server_name")+ " voting for " + candidateUID);
                conf.replace("last_term", candidateTerm);
                conf.replace("last_vote", candidateUID);
                config_object.writeJSON(conf);
                result.setSuccess(true);
                result.setTerm(candidateTerm); // Check if we need to increase term here.
                return result; //
                }
            else {
                conf.replace("last_term", candidateTerm);
                conf.replace("last_vote", "-1"); // and -1 is incompatible UID with my server from this moment.
                config_object.writeJSON(conf);
                result.setSuccess(false);
                result.setTerm(candidateTerm); // Check if we need to increase term here.
                return result; // term // Is this supposed to return old term?
            }
            }
        }

    @Override
    public RaftResult appendEntries(int leaderTerm, String leaderUID, int prevLogIndex, int prevLogTerm, String[] entries, Integer[] newLogTerms, int leaderCommit) {
        RaftConfig config_object = new RaftConfig();
        JSONObject conf = config_object.RaftConfig();

        System.out.println("Server "+ conf.get("server_name") + " received hearbeat from server "+leaderUID);
        this.resetLeaderTimeoutTimer();
        int term = (Integer) conf.get("last_term");
        if(leaderTerm>=term){
            conf.replace("last_term", leaderTerm);
            conf.replace("last_vote", "-1");
            config_object.writeJSON(conf);
        }
        JSONObject log = new RaftLog().RaftLog(conf.get("log_path").toString());
        JSONObject content_of_index = (JSONObject) log.get(prevLogIndex);
        int termAtIndex = (Integer) content_of_index.get("term");

        return null;
    }

    @Override
    public void handleTimeout(int timerID) {

    }

}
