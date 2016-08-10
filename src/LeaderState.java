import org.json.simple.JSONObject;


import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

/**
 * Created by allu on 9.8.2016.
 */
public class LeaderState extends BaseState {

    private Timer myHeartbeatTimeoutTimer;
    private Timer myCommitTimeoutTimer;
    private int Heartbeat_timeout_timer_id = 3;
    private int Commit_timeout_timer_id = 4;
    private JSONObject follower_status;


    @Override
    public void go() {
        synchronized (thred_lock){
            System.out.println("We're now the leader!");
            this.fabrikaattori();
            this.sendHeartbeats();
            myHeartbeatTimeoutTimer = scheduleTimer(timeout_min-50, this.Heartbeat_timeout_timer_id);
        }
    }

    private void sendHeartbeats() {
        System.out.println("Sending heartbeat!");
        repairLog();
    }

    private void repairLog() {
        RaftConfig config_object = new RaftConfig();
        JSONObject conf = config_object.RaftConfig();

        RaftResponses.setTerm(term);

        JSONObject servers = (JSONObject) conf.get("servers");
        JSONObject copy_of_servers = (JSONObject) servers.clone();
        boolean success = false;
        copy_of_servers.remove(conf.get("server_name").toString()); // Lets trust our own log.

        // Iterate trough servers
        for(Object server_name: copy_of_servers.keySet()){
            //Init stuff to send to follower node
            System.out.println("fetching last_term");
            int term = Integer.parseInt(conf.get("last_term").toString());
            System.out.println("fetching last_index");
            int last_index = Integer.parseInt(conf.get("last_index").toString());
            int first_index = 0;
            System.out.println("fetching last_commit");
            int last_commit = Integer.parseInt(conf.get("last_commit").toString());
            JSONObject log;
            JSONObject last_entry;
            ArrayList indexes = new ArrayList();
            log = new RaftLog().RaftLog(conf.get("log_path").toString());
            try{
                System.out.println("Fetching last_index("+last_index+")");

                indexes.addAll(log.keySet());
                Collections.sort(indexes);
                last_index = Integer.parseInt(indexes.get(indexes.size()-1).toString());
                first_index = Integer.parseInt(indexes.get(0).toString());
                System.out.println("Got last_index: "+last_index);
                //last_entry = (JSONObject) log.get(""+last_index);
                //System.out.println(last_entry.get("term").toString());

            } catch (NullPointerException e){
                System.out.println("Got null, reducing known last_index by one");
                //last_index = last_index-1;
            }
            conf.put("last_index", last_index);
            //conf.put("last_commit", last_index-1);
            config_object.writeJSON(conf);
            last_entry = (JSONObject) log.get(""+last_index);
            String[] values = new String[log.size()];
            Integer[] terms = new Integer[log.size()];
            String our_UID = conf.get("server_name").toString();
            values[0] = last_entry.get("entry").toString();
            terms[0] = (int)(long) last_entry.get("term");
            System.out.println("Attempting to correct log's");
            System.out.println("Send HeartBeat to: "+server_name.toString());
            this.remoteAppendEntries(server_name.toString(),
                    term,
                    our_UID,
                    last_index-1, // Index before the stuff we send
                    terms[0], // Term of first Index we send
                    values, // Values for all the indexes we send ([0] = first index we send)
                    terms, // Terms for all the values for all the indexes, like above.
                    last_commit); // Take a look at these values.
            System.out.println("Successfully sent Hearbeat");
            System.out.println("term:"+term+", UID:"+our_UID+", last_index-1:"+(last_index-1)+", terms[0]:"+terms[0]+", values:"+values[0]+", terms:"+terms[0]+", last_commit:"+last_commit);
/*            while(!success){ // Wait for response, true or false that may be.
                JSONObject replies = RaftResponses.getAppendResponses(term);
                try {
                    JSONObject resultti = (JSONObject) replies.get(server_name);
                    // If we get reply reset this timer, its meant only for lost connection.
                    success = (boolean) resultti.get("success"); // If we succeeded, move on to next node.
                    if(!success && last_index>first_index){
                        last_index--;
                        last_entry = (JSONObject) log.get(""+last_index);
                        values[0] = last_entry.get("entry").toString();
                        terms[0] = (int)(long) last_entry.get("term");
                        this.remoteAppendEntries(server_name.toString(),
                                term,
                                our_UID,
                                last_index-1, // Index before the stuff we send
                                terms[0], // Term of first Index we send
                                values, // Values for all the indexes we send ([0] = first index we send)
                                terms, // Terms for all the values for all the indexes, like above.
                                last_commit); // Take a look at these values.

                        //last_entry = (JSONObject)log.get(last_index);
                        //values[0] = last_entry.get("entry").toString();
                        //terms[0] = terms[0] = (int)(long) last_entry.get("term");
                    }
                } catch (NullPointerException ignored){
                }*/
            }
/*            while(!success){ // If the request hasn't succeeded, do something about it

                JSONObject replies = RaftResponses.getAppendResponses(term);
                 // Check results for 50 times before giving up and moving on.
                    try {
                        JSONObject resultti = (JSONObject) replies.get(server_name);
                        // If we get reply reset this timer, its meant only for lost connection.
                        success = (boolean) resultti.get("success"); // If we succeeded, move on to next node.
                        if(!success){
                            last_index--;
                            last_entry = (JSONObject)log.get(last_index);
                            values[0] = last_entry.get("entry").toString();
                            terms[0] = terms[0] = (int)(long) last_entry.get("term");
                        }
                    } catch (NullPointerException ignored){
                        System.out.println("Didn't receive a response yet.");
                    }

                }*/



        //int last_commit = (int)(long) conf.get("last_commit");
        conf.put("last_commit", last_index); // Once we've updated all the nodes to our latest log, update commit log
        //config_object = new RaftConfig();
        config_object.writeJSON(conf);
    }

    @Override
    public RaftResult requestVote(int candidateTerm, String candidateUID, int lastLogIndex, int lastLogTerm) {
        synchronized (thred_lock){
            RaftConfig conf_object = new RaftConfig();
            JSONObject conf = conf_object.RaftConfig();
            term = (int)(long) conf.get("last_term");
            RaftResultImp reply = new RaftResultImp();
            if(candidateTerm>term){
                try{this.myHeartbeatTimeoutTimer.cancel();} catch (NullPointerException ignored){}
                RaftServer.setState(new FollowerState());
                reply.setSuccess(true);
                reply.setTerm(candidateTerm);
                conf.put("last_term", candidateTerm);
                conf_object.writeJSON(conf);
                return reply;
            }
            reply.setSuccess(false);
            reply.setTerm(term);
            return reply;
        }
    }
    public void fabrikaattori(){
        RaftConfig conf_object = new RaftConfig();
        RaftLog log_object = new RaftLog();
        JSONObject conf = conf_object.RaftConfig();
        JSONObject log = log_object.RaftLog(conf.get("log_path").toString());

        //increase term as we append to our log
        int term = (int)(long) conf.get("last_term");
        term++;
        conf.put("last_term", term);
        term = (int) conf.get("last_term");
        //add log entry
        JSONObject content = new JSONObject();
        int new_index = (int)(long) conf.get("last_index");
        new_index++;
        content.put("term", term);
        content.put("entry", term+5);
        log.put(new_index, content);

        // update last_index to conf
        conf.put("last_index", new_index);

        //Write stuff to disk
        conf_object.writeJSON(conf);
        log_object.writeJSON(log);

    }
    @Override
    public RaftResult appendEntries(int leaderTerm, String leaderUID, int prevLogIndex, int prevLogTerm, String[] entries, Integer[] newLogTerms, int leaderCommit) {
        synchronized (thred_lock){
            RaftConfig conf_object = new RaftConfig();
            JSONObject conf = conf_object.RaftConfig();
            term = Integer.parseInt(conf.get("last_term").toString());
            RaftResultImp reply = new RaftResultImp();
            if(leaderTerm>term){
                this.myHeartbeatTimeoutTimer.cancel();
                RaftServer.setState(new FollowerState());
                reply.setSuccess(true);
                reply.setTerm(leaderTerm);
                conf.put("last_term", leaderTerm);
                conf_object.writeJSON(conf);
                return reply;
            }
            reply.setSuccess(false);
            reply.setTerm(term);
            return reply;
        }
    }

    @Override
    public void handleTimeout(int timerID) {
        synchronized (thred_lock){
            if(timerID==this.Heartbeat_timeout_timer_id){
                myHeartbeatTimeoutTimer = scheduleTimer(timeout_min-50, this.Heartbeat_timeout_timer_id);
                this.fabrikaattori();
                this.sendHeartbeats();
            }
        }
    }
}
