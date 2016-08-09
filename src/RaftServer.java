import raft.Raft;
import raft.RaftResult;

import java.rmi.RemoteException;

/**
 * Created by allu on 9.8.2016.
 */
public class RaftServer implements Raft {
    private static BaseState cState;
    public static void setMode(BaseState state){
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
