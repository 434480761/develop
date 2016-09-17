package nd.esp.service.lifecycle.services.repository.v06;

import nd.esp.service.lifecycle.models.v06.ResRepositoryModel;
/**
 * 物理存储空间Service
 * <p>Create Time: 2015年7月16日           </p>
 * @author xiezy
 */
public interface ResRepositoryService {
    
    /**
     * 申请物理资源存储空间   
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param repositoryViewModel
     * @param validResult
     * @return
     */
    public ResRepositoryModel createRepository(ResRepositoryModel repositoryModel);
    
    /**
     * 获取物理空间信息 
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param type      存储空间的类型，Org代表组织机构
     * @param target    目标的标识
     * @return
     */
    public ResRepositoryModel getRepositoryDetailByCondition(String type,String target);
    
    /**
     * 通过ID获取物理空间信息
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param id    私有空间的id标识
     * @return
     */
    public ResRepositoryModel getRepositoryDetailById(String id);
    
    /**
     * 修改资源物理空间信息   
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param id    私有空间的id标识
     * @param repositoryViewModelForUpdate
     * @param validResult
     * @return
     */
    public ResRepositoryModel updateRepository(ResRepositoryModel repositoryModel);
    
    /**
     * 删除资源物理空间信息
     * <p>Create Time: 2015年7月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param type
     * @param target
     * @return
     */
    public boolean deleteRepository(ResRepositoryModel repositoryModel);
}
