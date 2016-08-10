

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by allu on 9.8.2016.
 */
public class RaftServer extends UnicastRemoteObject
        implements Raft {
    private static BaseState cState;

    public RaftServer() throws RemoteException{}
    public static void setState(BaseState state){
        System.out.println("Changing State.");
        cState = state;
        cState.go();
    }

    @Override
    public RaftResult appendEntries(Integer term, String leaderID, int prevLogIndex, Integer prevLogTerm, String[] newLogEntries, Integer[] newLogTerms, int leaderCommit) throws RemoteException {
        return cState.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, newLogEntries, newLogTerms, leaderCommit);
    }

    public RaftResult requestVote(Integer candidateTerm, String candidateUID, int lastLogIndex, Integer lastLogTerm) throws RemoteException {
        return cState.requestVote(candidateTerm, candidateUID, lastLogIndex, lastLogTerm);
    }
}
