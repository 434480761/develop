package nd.esp.service.lifecycle.support.busi.titan.tranaction;

import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2016/9/12.
 */
@Component
public class TitanSubmitTransactionImpl implements TitanSubmitTransaction {
    @Override
    public boolean submit(TitanTransaction transaction) {

        return false;
    }
}
