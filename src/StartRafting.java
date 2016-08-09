import org.json.simple.JSONObject;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by allu on 9.8.2016.
 */
public class StartRafting {

    public void main (String[] args){
        JSONObject conffi = new RaftConfig().RaftConfig();
        String UID = conffi.get("server_name").toString();
        JSONObject servers = (JSONObject) conffi.get("servers");
        String url = servers.get(UID).toString();

        JSONObject log = new RaftLog().RaftLog(conffi.get("log_path").toString());
        int lastTerm = (Integer) conffi.get("last_term");
        int lastApplied = (Integer) conffi.get("last_index");
        RaftResponses.init(lastTerm);

        try{
            BaseState.init_server(conffi, log);
            RaftServer server = new RaftServer();
            RaftServer.setMode(new FollowerState());

            Naming.rebind(url, server);
        } catch (MalformedURLException me){
            System.out.println("Caught malformed url");
            me.printStackTrace();

        } catch (RemoteException re){
            System.out.println("Caught RemoteException");
            re.printStackTrace();
        }
    }

}
