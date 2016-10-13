package nd.esp.service.lifecycle.support.busi.titan.tranaction;

/**
 * Created by Administrator on 2016/9/12.
 */
public interface TitanSubmitTransaction {
    public void submit(TitanTransaction transaction);
    public boolean submit4Sync(TitanTransaction transaction);
}
