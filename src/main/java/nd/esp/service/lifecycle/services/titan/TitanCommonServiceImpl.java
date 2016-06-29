package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by liuran on 2016/6/13.
 */
@Repository
public class TitanCommonServiceImpl implements TitanCommonService {
    public static final String EDGE="edge";
    public static final String VERTEX="vertex";

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public void delete(String type, String id) {
        if(type==EDGE){
            titanCommonRepository.deleteEdgeById(id);
        } else if(type==VERTEX){

        }

    }

    @Override
    public void batchDelete(String type, List<String> ids) {
        if(type==EDGE){
            titanCommonRepository.butchDeleteEdgeById(ids);
        } else if(type==VERTEX){

        }
    }
}
