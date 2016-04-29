package nd.esp.service.lifecycle.services.ebooks.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.EbookModel;
import nd.esp.service.lifecycle.services.ebooks.v06.EbookService;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 电子教材业务实现类
 * 
 * @author linsm
 */
@Service("ebookServiceV06")
@Transactional
public class EbookServiceImpl implements EbookService {
    @Autowired
    private NDResourceService ndResourceService;

    @Override
    public EbookModel create(EbookModel ebookModel) {
        return (EbookModel) ndResourceService.create(ResourceNdCode.ebooks.toString(), ebookModel);
    }

    @Override
    public EbookModel update(EbookModel ebookModel) {
        return (EbookModel) ndResourceService.update(ResourceNdCode.ebooks.toString(), ebookModel);
    }

}
