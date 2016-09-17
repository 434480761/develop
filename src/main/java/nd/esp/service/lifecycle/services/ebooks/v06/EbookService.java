package nd.esp.service.lifecycle.services.ebooks.v06;

import nd.esp.service.lifecycle.models.v06.EbookModel;

/**
 * 电子教材业务层接口
 * 
 * @author linsm
 * @version 0.6
 */
public interface EbookService {
    /**
     * 电子教材创建
     * 
     * @param ebookModel
     * @return
     */
    public EbookModel create(EbookModel ebookModel);

    /**
     * 电子教材修改
     * 
     * @param ebookModel
     * @return
     */
    public EbookModel update(EbookModel ebookModel);

    EbookModel patch(EbookModel ebookModel);
}