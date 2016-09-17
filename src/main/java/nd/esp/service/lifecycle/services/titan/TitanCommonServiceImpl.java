package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.daos.titan.TitanCoverageRepositoryImpl;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.daos.titan.inter.TitanCoverageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by liuran on 2016/6/13.
 */
@Repository
public class TitanCommonServiceImpl implements TitanCommonService {
    private final static Logger LOG = LoggerFactory
            .getLogger(TitanCoverageRepositoryImpl.class);
    public static final String EDGE="edge";
    public static final String VERTEX="vertex";

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Autowired
    private TitanCoverageRepository titanCoverageRepository;

    @Override
    public void delete(String type, String id) {
        if(EDGE.equals(type)){
            try {
                titanCoverageRepository.delete(id);
            } catch (Exception e) {
                LOG.error("titan_repository error:{}" ,e.getMessage());
                //TODO 出现异常处理方式，重新执行这条sql语句
            }
        } else if(type==VERTEX){

        }

    }

    @Override
    public void batchDelete(String type, List<String> ids) {
        if(EDGE.equals(type)){
            try {
                titanCommonRepository.butchDeleteEdgeById(ids);
            } catch (Exception e) {
                LOG.error("titan_repository error:{}" ,e.getMessage());
                //TODO 出现异常处理方式，重新执行这条sql语句
            }
        } else if(type==VERTEX){

        }
    }
}
