import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import java.nio.channels.Pipe;
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
        int last_term = (int)(long) config.get("last_term");
        config.put("last_term", last_term+1);
        config_object.writeJSON(config);
    }

    private void beginElection() {
        //System.out.println("We're starting elections!");
        RaftConfig config_object = new RaftConfig();
        JSONObject config = config_object.RaftConfig();
        int last_term = (int)(long) config.get("last_term");
        System.out.println("We are a candidate on term: "+last_term);
        String our_name = config.get("server_name").toString();
        int last_applied = (int)(long) config.get("last_index");
        RaftResponses.setTerm(last_term);
        RaftResponses.clearVotes(last_term);

        Random rand = new Random();
        myElectionTimeoutTimer = scheduleTimer(rand.nextInt(timeout_max-timeout_min)+timeout_min, this.Election_timeout_timer_id);


        config_object = new RaftConfig();
        JSONObject servers = (JSONObject) config_object.RaftConfig().get("servers");
        boolean voted_us= false;
        for(Object server_name: servers.keySet()){
            if(!voted_us) {
                System.out.println("Voting ourselves first.");
                this.remoteRequestVote(our_name, last_term, our_name, last_applied, last_term - 1);
                voted_us = true;
            }
            if(!Objects.equals(server_name.toString(), our_name)){
            System.out.println("Requesting vote from "+server_name.toString());
            this.remoteRequestVote(server_name.toString(), last_term, our_name, last_applied, last_term-1);
        }
        }

    }

    @Override
    public RaftResult requestVote(int candidateTerm, String candidateUID, int lastLogIndex, int lastLogTerm) {
        synchronized (thred_lock){
            JSONObject conf = new RaftConfig().RaftConfig();
            int term = (int)(long) conf.get("last_term");
            System.out.println("Current term while vote request arrived:"+term);
            String server_name = conf.get("server_name").toString();
            RaftResultImp reply = new RaftResultImp();
            String voted = conf.get("last_vote").toString();
            if(candidateTerm>term){
                System.out.println("Candidate("+candidateUID+")has higher term("+candidateTerm+") then us. Submitting to their will.");
                System.out.println("Resetting our timer so our vote isn't meaningless");
                myElectionTimeoutTimer.cancel();
                Random rand = new Random();
                myElectionTimeoutTimer = scheduleTimer(rand.nextInt(timeout_max-timeout_min)+timeout_min, this.Election_timeout_timer_id);
                conf.put("last_term", candidateTerm);
                conf.put("last_vote", candidateUID);
                RaftConfig config_object = new RaftConfig();
                config_object.writeJSON(conf);
                reply.setSuccess(true);
                reply.setTerm(candidateTerm);
                System.out.println(reply.getTerm());
                return reply;
            }
            if(!voted.equals("-1")){
                System.out.println("We have already gave a vote this term, return false.");
                reply.setSuccess(false);
                reply.setTerm(term);
                System.out.println(reply.getTerm());
                return reply;
            }
            if(Objects.equals(candidateUID, server_name)){
                System.out.println("Always vote for ourselves first.");
                reply.setSuccess(true);
                reply.setTerm(term);
                System.out.println(reply.getTerm());
                return reply;
            }
            else{
                reply.setSuccess(true);
                reply.setTerm(term);
                System.out.println(reply.getTerm());
                return reply;
            }
        }
    }

    @Override
    public RaftResult appendEntries(int leaderTerm, String leaderUID, int prevLogIndex, int prevLogTerm, String[] entries, Integer[] newLogTerms, int leaderCommit) {
        RaftResultImp reply = new RaftResultImp();
        synchronized (thred_lock){
            JSONObject conf = new RaftConfig().RaftConfig();
            int term = (int)(long) conf.get("last_term");
            System.out.println("We received heartbeat from "+ leaderUID);

            if(leaderTerm>term){
                this.myElectionTimeoutTimer.cancel();
                RaftConfig config_object = new RaftConfig();
                conf.put("last_term", leaderTerm);
                conf.put("last_vote", "-1");
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
        System.out.println("Timeout: "+timerID);
        synchronized (thred_lock){
            if(timerID==this.Election_timeout_timer_id){
                System.out.println("Hellow");
                this.myElectionTimeoutTimer.cancel();
                JSONObject conf = new RaftConfig().RaftConfig();
                int current_term = (int)(long) conf.get("last_term");
                JSONObject votes = RaftResponses.getVotes(current_term);
                JSONObject servers = (JSONObject) conf.get("servers");
                Integer server_count = servers.size();
                Integer vote_counter = 0;
                try{
                System.out.println(votes.keySet());

                for(Object server_name: votes.keySet()){
                    boolean vote_value = (boolean) votes.get(server_name);
                    if(vote_value){
                        vote_counter++;
                    }
                    System.out.println("Counted "+ vote_counter+"/"+server_count+" votes");
                }
                    if(vote_counter>server_count/2){
                        RaftServer.setState(new LeaderState());
                    }
                    else{
                        this.incrementTerm();
                        this.beginElection();
                    }
                } catch (NullPointerException e){
                    System.out.println("Got no votes, beginning another round");
                    this.incrementTerm();
                    this.beginElection();
                }
            }
            }

        }

    }

