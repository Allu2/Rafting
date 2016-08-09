import org.json.simple.JSONObject;
import raft.RaftResult;

import java.util.Objects;
import java.util.Random;
import java.util.Timer;

/**
 * Created by allu on 9.8.2016.
 */
public class CandidateState extends BaseState {

    private Timer myElectionTimeoutTimer;
    private int Election_timeout_timer_id = 2;

    public void go(){
        synchronized (thred_lock){
            this.incrementTerm();
            System.out.println("Switched to candidate mode.");

            this.beginElection();
        }
    }


    private void incrementTerm(){
        RaftConfig config_object = new RaftConfig();
        JSONObject config = config_object.RaftConfig();
        int last_term = (Integer) config.get("last_term");
        config.put("last_term", last_term+1);
        config_object.writeJSON(config);
    }

    private void beginElection() {
        System.out.println("We're starting elections!");
        RaftConfig config_object = new RaftConfig();
        JSONObject config = config_object.RaftConfig();
        int last_term = (Integer) config.get("last_term");
        String our_name = config.get("server_name").toString();
        int last_applied = (Integer) config.get("last_index");
        RaftResponses.setTerm(last_term);
        RaftResponses.clearVotes(last_term);

        Random rand = new Random();
        myElectionTimeoutTimer = scheduleTimer(rand.nextInt(timeout_max-timeout_min)+timeout_min, this.Election_timeout_timer_id);


        config_object = new RaftConfig();
        JSONObject servers = (JSONObject) config_object.RaftConfig().get("servers");
        for(Object server_name: servers.keySet()){
            this.remoteRequestVote(server_name.toString(), last_term, our_name, last_applied, last_term-1);
        }

    }

    @Override
    public RaftResult requestVote(int candidateTerm, String candidateUID, int lastLogIndex, int lastLogTerm) {
        synchronized (thred_lock){
            JSONObject conf = new RaftConfig().RaftConfig();
            int term = (Integer) conf.get("last_term");
            String server_name = conf.get("server_name").toString();
            RaftResultImp reply = new RaftResultImp();
            if(Objects.equals(candidateUID, server_name)){
                reply.setSuccess(true);
                reply.setTerm(term);
                return reply;
            }
            else{
                reply.setSuccess(false);
                reply.setTerm(term);
                return reply;
            }
        }
    }

    @Override
    public RaftResult appendEntries(int leaderTerm, String leaderUID, int prevLogIndex, int prevLogTerm, String[] entries, Integer[] newLogTerms, int leaderCommit) {
        RaftResultImp reply = new RaftResultImp();
        synchronized (thred_lock){
            JSONObject conf = new RaftConfig().RaftConfig();
            int term = (Integer) conf.get("last_term");
            System.out.println("We received heartbeat from "+ leaderUID);

            if(leaderTerm>term){
                this.myElectionTimeoutTimer.cancel();
                RaftConfig config_object = new RaftConfig();
                conf.put("last_term", leaderTerm);
                config_object.writeJSON(conf);
                RaftServer.setState(new FollowerState());

                reply.setSuccess(true);
                reply.setTerm(leaderTerm);
                return reply;
            }
            else{
                reply.setSuccess(false);
                reply.setTerm(term);
            }
        }

        return null;
    }

    @Override
    public void handleTimeout(int timerID) {
        synchronized (thred_lock){
            if(timerID==this.Election_timeout_timer_id){
                this.myElectionTimeoutTimer.cancel();
                JSONObject conf = new RaftConfig().RaftConfig();
                int current_term = (Integer) conf.get("last_term");
                JSONObject votes = RaftResponses.getVotes(current_term);
            }

        }

    }
}