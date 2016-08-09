
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Raft extends Remote
{
    RaftResult appendEntries(Integer term, String leaderID, int prevLogIndex, Integer prevLogTerm, String[] newLogEntries, Integer[] newLogTerms, int leaderCommit) throws RemoteException;
    RaftResult requestVote(Integer term, String candidateID, int lastLogIndex, Integer lastLogTerm) throws RemoteException;
}
