import java.io.Serializable;

/**
 * Created by allu on 9.8.2016.
 */
public class RaftResultImp extends RaftResult implements Serializable{

    protected boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    protected boolean log_is_consistent;

    public void setLog_is_consistent(boolean log_is_consistent) {
        this.log_is_consistent = log_is_consistent;
    }


    public void setTerm(int new_term){
        this.term = new_term;
    }

    @Override
    public boolean isSuccessful() {
        return this.success;
    }

    @Override
    public boolean isLogConsistent() {
        return this.log_is_consistent;
    }
}
