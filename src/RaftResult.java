
public abstract class RaftResult
{
    protected int term;

    public int getTerm()
    {
        return term;
    }
    
    abstract public boolean isSuccessful();
    abstract public boolean isLogConsistent();
}
