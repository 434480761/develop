package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.support.busi.titan.TitanTreeModel;

/**
 * Created by liuran on 2016/6/7.
 */
public interface TitanTreeMoveService {
    public void addNode(TitanTreeModel titanTreeModel);
    public void moveNode(TitanTreeModel titanTreeModel);
}
