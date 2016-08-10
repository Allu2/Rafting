import org.json.simple.JSONObject;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Created by allu on 9.8.2016.
 */
class StartRafting {

    public static void main (String[] args){
        System.out.println("Starting Raft implementation Rafting.");
        System.out.println("Cleaning possible mess from unclean exit.");
        RaftConfig config_object = new RaftConfig();
        JSONObject conffi = new RaftConfig().RaftConfig();
        conffi.put("last_vote", "-1");
        config_object.writeJSON(conffi);
        String UID = conffi.get("server_name").toString();
        String url = conffi.get("url").toString();

        JSONObject log = new RaftLog().RaftLog(conffi.get("log_path").toString());
        int lastTerm = (int)(long) conffi.get("last_term");
        int lastApplied = (int)(long) conffi.get("last_index");
        System.out.println("Init RaftResponses with last term.");
        RaftResponses.init(lastTerm);

        try{
            System.out.printf("Setup BaseState");
            BaseState.init_server(conffi, log);
            RaftServer server = new RaftServer();
            System.out.printf("Set node to FollowerState");
            RaftServer.setState(new FollowerState());
            System.out.printf("Bind node to RMI server");
            Naming.rebind(url, server);
        } catch (MalformedURLException me){
            System.out.println("Caught malformed url");
            me.printStackTrace();

        } catch(ConnectException ignored){

        } catch (RemoteException re){
            System.out.println("Caught RemoteException");
            re.printStackTrace();
        }
    }

}
