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
        JSONObject conffi = new RaftConfig().RaftConfig();
        String UID = conffi.get("server_name").toString();
        String url = conffi.get("url").toString();

        JSONObject log = new RaftLog().RaftLog(conffi.get("log_path").toString());
        int lastTerm = (int)(long) conffi.get("last_term");
        int lastApplied = (int)(long) conffi.get("last_index");
        RaftResponses.init(lastTerm);

        try{
            BaseState.init_server(conffi, log);
            RaftServer server = new RaftServer();
            RaftServer.setState(new FollowerState());

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
