import org.json.simple.JSONObject;


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
            System.out.println("Switched to follower state.");
            Random rand = new Random();
            System.out.println("Start timer for switching to Candidate state.");
            myLeaderTimeoutTimer = scheduleTimer(rand.nextInt(timeout_max)+timeout_min, this.Leader_timeout_timer_id);
        }
    }

    private void resetLeaderTimeoutTimer(){
        System.out.println("Resetting timeout before switching to Candidate");
        myLeaderTimeoutTimer.cancel();
        Random rand = new Random();
        myLeaderTimeoutTimer = scheduleTimer(rand.nextInt(timeout_max)+timeout_min, this.Leader_timeout_timer_id);
    }

    public RaftResult requestVote(int candidateTerm,
                                  String candidateUID,
                                  int lastLogIndex,
                                  int lastLogTerm){
        System.out.println("Locking at requestVote");
        synchronized (thred_lock){

            RaftConfig config_object = new RaftConfig();
            JSONObject conf = config_object.RaftConfig();
            int term = (int)(long) conf.get("last_term");
            String last_vote = (String) conf.get("last_vote");
            int last_index = (int)(long) conf.get("last_index");
            RaftResultImp result = new RaftResultImp();

            System.out.println("If candidateTerm("+candidateTerm+") is larger or equal to our term("+term+") " +
                    "and last_vote is '-1'("+last_vote+") " +
                    "and lastLogIndex("+lastLogIndex+") is larger then last_index("+last_index+")");

            if(candidateTerm > term && last_vote.equals("-1") && lastLogIndex>=last_index){ // Using magic value to get stuff done :P
                System.out.println("Resetting timeout before we turn to candidate");
                this.resetLeaderTimeoutTimer();
                System.out.println("We give our vote to "+candidateUID);
                //System.out.println("Server "+ conf.get("server_name")+ " voting for " + candidateUID);
                System.out.println("We set out last_term("+term+" to Candidates term("+candidateTerm+")");
                conf.replace("last_term", candidateTerm);
                System.out.println("We set our last_vote to Candidates UID("+candidateUID+")");
                conf.replace("last_vote", candidateUID);
                config_object.writeJSON(conf);
                System.out.println("We reply with success true.");
                result.setSuccess(true);
                result.setTerm(candidateTerm); // Check if we need to increase term here.
                return result; //
                }
            else {
                System.out.println("Earlier check failed and we return false. last_vote is set to '-1'");
                //System.out.println("Updating term to candidates: "+candidateTerm);
                //conf.replace("last_term", candidateTerm);
                conf.replace("last_vote", "-1"); // and -1 is incompatible UID with my server from this moment.
                config_object.writeJSON(conf);
                result.setSuccess(false);
                result.setTerm(term); // Check if we need to increase term here.
                return result; // term // Is this supposed to return old term?
            }
            }
        }

    @Override
    public RaftResult appendEntries(int leaderTerm, String leaderUID, int prevLogIndex, int prevLogTerm, String[] entries, Integer[] newLogTerms, int leaderCommit) {
        System.out.println("term:"+leaderTerm+", UID:"+leaderUID+", last_index-1:"+prevLogIndex+", terms[0]:"+prevLogTerm+", values:"+entries[0]+", terms:"+newLogTerms[0]+", last_commit:"+leaderCommit);
        RaftConfig config_object = new RaftConfig();
        JSONObject conf = config_object.RaftConfig();
        synchronized (thred_lock){
            RaftResultImp result = new RaftResultImp();
            System.out.println("Server "+ conf.get("server_name") + " received heartbeat from server "+leaderUID);
            System.out.println("Resetting Timeout counter");
            this.resetLeaderTimeoutTimer();
            int term = Integer.parseInt(conf.get("last_term").toString());

            System.out.println("If leader term("+leaderTerm+") is equal or larger then our term("+term+")");
            if(leaderTerm>=term){
                System.out.println("Set last_term to leader's term and last_vote to '-1'");
                conf.replace("last_term", leaderTerm);
                conf.replace("last_vote", "-1");
                config_object.writeJSON(conf);
            }
            RaftLog log_object = new RaftLog();
            JSONObject log = log_object.RaftLog(conf.get("log_path").toString());

            JSONObject content_of_index = (JSONObject) log.get(""+prevLogIndex);
            int termAtIndex=0;
            try{
                termAtIndex = Integer.parseInt(content_of_index.get("term").toString());
            } catch (NullPointerException e){
                System.out.printf("Failed to find content in the given index.");
                //JSONObject content = new JSONObject();
                //content.put("term", )
                //log.put(""+(prevLogIndex+1), )
            }
            int counter = 0;
            System.out.println("If our term in index "+prevLogIndex+" matches to "+prevLogTerm+ " we are ready to receive more logs.");
            if(termAtIndex==prevLogTerm){
                System.out.println("Term and index matched.");
                JSONObject content = new JSONObject();
                content.put("entry", entries[0]);
                content.put("term", newLogTerms[0]);
                log.put((prevLogIndex+1), content);
                log_object.writeJSON(log);

/*                for(String entry: entries){ // naive expectations are being made in this loop
                    System.out.println("entries size: "+entries.length);
                    System.out.println("Adding term:"+newLogTerms[counter]+" and entry: "+entry+" on index "+(prevLogIndex+counter+1));
                    content.put("term", newLogTerms[counter]);
                    content.put("entry", entry);
                    log.put(prevLogIndex+counter+1, content); // prevLog is one before what we get, counter starts from 0
                    log_object.writeJSON(log);
                    counter++;
                }*/
                result.setTerm(leaderTerm);
                result.setSuccess(true);
                result.setLog_is_consistent(true);
                return result;
            }
            else{
                System.out.println("Term and index didn't match");
                System.out.println("On index("+prevLogIndex+") we have term ("+termAtIndex+")");
                System.out.println("Fixing log..");
                JSONObject content = new JSONObject();
                content.put("entry", entries[0]);
                content.put("term", newLogTerms[0]);
                log.put((prevLogIndex+1), content);
                log_object.writeJSON(log);
/*                for(String entry: entries){ // naive expectations are being made in this loop
                    System.out.println("entries size: "+entries.length);
                    System.out.println("Adding term:"+newLogTerms[counter]+" and entry: "+entry+" on index "+(prevLogIndex+counter+1));
                    content.put("term", newLogTerms[counter]);
                    content.put("entry", entry);
                    log.put(prevLogIndex+counter+1, content); // prevLog is one before what we get, counter starts from 0
                    log_object.writeJSON(log);
                    counter++;
                }*/
                System.out.println("Replaced new stuff index with leader's copy.");
                System.out.println("Setting result term to our term.");
                result.setTerm(term);
                System.out.println("Returning false.");
                result.setSuccess(false);
                result.setLog_is_consistent(false);
                return result;
            }
     }
    }

    @Override
    public void handleTimeout(int timerID) {
        synchronized (thred_lock){
            if(timerID==this.Leader_timeout_timer_id){
                myLeaderTimeoutTimer.cancel();
                System.out.println("Timeout! Time to Rise up!(Candidate mode activated!)");
                RaftServer.setState(new CandidateState());
            }
        }
    }

}
