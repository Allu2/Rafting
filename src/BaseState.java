import org.json.simple.JSONObject;
import raft.RaftResult;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by aleksi on 8.8.2016.
 */
public abstract class BaseState {
    private static JSONObject config;
    private static JSONObject log;

    protected static int timeout_min;
    protected static int timeout_max;
    protected static int term;
    protected static int last_index;
    protected static String UID;
    protected static Object thred_lock;


    public static void init_server (JSONObject conffi,
                                    JSONObject log)
    {
        //Load conffi to the state
        config = conffi;

        // HeartBeat is between these.
        timeout_min = (Integer)conffi.get("timeout_min");
        timeout_max = (Integer)conffi.get("timeout_max");

        // Get our UNIQUE name from config.
        UID = conffi.get("server_name").toString();

        // Lock object
        thred_lock = new Object();

        // Set term from last known in config.
        term = (Integer) conffi.get("last_term");

        // Set last index in log from config (Cause our log isn't sorted :P )
        last_index = (Integer) conffi.get("last_index");

        System.out.println("Server initialized");

    }

    // Basicly copy paste from https://github.com/ngbalk/Raft/blob/master/src/edu/duke/raft/RaftMode.java
    // Which has been largely base of structure for this implementation
    protected final Timer scheduleTimer(long millis, final int timerID){
        Timer timer = new Timer(false);
        TimerTask task = new TimerTask(){
            public void run(){
                BaseState.this.handleTimeout(timerID);
            }
        };
        timer.schedule(task, millis);
        return timer;
    }

    private final String getRmiUrl(String UID){
        // Needs testing.
        JSONObject server_list = (JSONObject)config.get("servers");
        return (String) server_list.get(UID);
    }

    protected final void remoteRequestVote(final String UID,
                                           final int candidateTerm,
                                           final String candidateUID,
                                           final int lastLogIndex,
                                           final int lastLogTerm){
        new Thread(){
            public void run(){
                String url = getRmiUrl(UID);
                try{
                    RaftServer server = (RaftServer) Naming.lookup(url);
                    RaftResult response = server.requestVote(candidateTerm,
                                                            candidateUID,
                                                            lastLogIndex,
                                                            lastLogTerm);

                    synchronized (BaseState.thred_lock){
                        RaftResponses.setVote(UID,
                                              response,
                                              candidateTerm);
                    }
                } catch (MalformedURLException me){
                    System.out.println("Caught malformed url");
                    me.printStackTrace();

                } catch (RemoteException re){
                    System.out.println("Caught RemoteException");
                    re.printStackTrace();
                } catch (NotBoundException nb){
                    System.out.println("Caught NotBoundException");
                    nb.printStackTrace();
                }
            }
        }.start();
    }
    protected  final void remoteAppendEntries(final String UID,
                                              final int leaderTerm,
                                              final String leaderID,
                                              final int prevLogIndex,
                                              final int prevLogTerm,
                                              final String[] entries,  // Match with newLogTerms I suppose.
                                              final Integer[] newLogTerms,
                                              final int leaderCommit){
        new Thread(){
            public void run(){
                String url = getRmiUrl(UID);
                try{
                    RaftServer server = (RaftServer) Naming.lookup(url);
                    RaftResult response = server.appendEntries(leaderTerm,
                                                               leaderID,
                                                               prevLogIndex,
                                                               prevLogTerm,
                                                               entries,
                                                               newLogTerms,
                                                               leaderCommit);
                    synchronized (BaseState.thred_lock){
                        RaftResponses.setAppendResponse(UID, response, leaderTerm);
                    }



                } catch (MalformedURLException me){
                    System.out.println("Caught malformed url");
                    me.printStackTrace();

                } catch (RemoteException re){
                    System.out.println("Caught RemoteException");
                    re.printStackTrace();
                } catch (NotBoundException nb){
                    System.out.println("Caught NotBoundException");
                    nb.printStackTrace();
                }
            }
        }.start();
    }
    abstract public void go ();

    abstract  public  RaftResult requestVote(int candidateTerm,
                                      String candidateUID,
                                      int lastLogIndex,
                                      int lastLogTerm);

    abstract public RaftResult appendEntries(int leaderTerm,
                                      String leaderUID,
                                      int prevLogIndex,
                                      int prevLogTerm,
                                      String[] entries,
                                      Integer[] newLogTerms,
                                      int leaderCommit);

    abstract public void handleTimeout(int timerID);
}
