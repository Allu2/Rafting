import org.json.simple.JSONObject;


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
            this.sendHeartbeats();
            myHeartbeatTimeoutTimer = scheduleTimer(timeout_min-50, this.Heartbeat_timeout_timer_id);
        }
    }

    private void sendHeartbeats() {
        System.out.println("Sending heartbeat!");
        repairLog();
    }

    private void repairLog() {
        JSONObject conf = new RaftConfig().RaftConfig();

        RaftResponses.setTerm(term);

        JSONObject servers = (JSONObject) conf.get("servers");


        boolean success = false;
        for(Object server_name: servers.keySet()){

            //Init stuff to send to follower node
            int term = (int)(long) conf.get("last_term");
            int last_index = (int)(long) conf.get("last_index");
            int last_commit = (int)(long) conf.get("last_commit");
            JSONObject log = new RaftLog().RaftLog(conf.get("log_path").toString());
            JSONObject last_entry = (JSONObject)log.get(last_index);
            String[] values = new String[log.size()];
            Integer[] terms = new Integer[log.size()];
            String our_UID = conf.get("server_name").toString();
            values[0] = last_entry.get("entry").toString();
            terms[0] = (int)(long) last_entry.get("term");

            while(!success){ // If the request hasn't succeeded, do something about it
                remoteAppendEntries(server_name.toString(),
                        term,
                        our_UID,
                        last_index-1, // Index before the stuff we send
                        terms[0], // Term of first Index we send
                        values, // Values for all the indexes we send ([0] = first index we send)
                        terms, // Terms for all the values for all the indexes, like above.
                        last_commit); // Take a look at these values.
                JSONObject replies = RaftResponses.getAppendResponses(term);
                int magic_wait_timer = 50;
                while(magic_wait_timer>0){ // Check results for 50 times before giving up and moving on.
                    try {
                        JSONObject resultti = (JSONObject) replies.get(server_name);
                        magic_wait_timer = 50; // If we get reply reset this timer, its meant only for lost connection.
                        success = (boolean) resultti.get("success"); // If we succeeded, move on to next node.
                        if(!success){
                            last_index--;
                            last_entry = (JSONObject)log.get(last_index);
                            values[0] = last_entry.get("entry").toString();
                            terms[0] = terms[0] = (int)(long) last_entry.get("term");
                        }
                    } catch (NullPointerException ignored){
                    }
                    magic_wait_timer--;
                }
            }

        }
        //int last_commit = (int)(long) conf.get("last_commit");
        conf.put("last_commit", last_index); // Once we've updated all the nodes to our latest log, update commit log
        RaftConfig config_object = new RaftConfig();
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
                this.myHeartbeatTimeoutTimer.cancel();
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
        conf.put("last_term", term+1);
        term = (int)(long) conf.get("last_term");
        //add log entry
        JSONObject content = new JSONObject();
        int new_index = (int)(long) conf.get("last_index")+1;
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
            term = (int)(long) conf.get("last_term");
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
