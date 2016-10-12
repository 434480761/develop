package nd.esp.service.lifecycle.services.examinationpapers.v06.impl;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.ExaminationPaperModel;
import nd.esp.service.lifecycle.services.examinationpapers.v06.ExaminationPaperServiceV06;
import nd.esp.service.lifecycle.support.DbName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 试卷业务实现类
 * 
 * @author xuzy
 */
@Service("examinationPaperServiceV06")
@Transactional
public class ExaminationPaperServiceImplV06 implements ExaminationPaperServiceV06 {
    @Autowired
    private NDResourceService ndResourceService;

    @Override
    public ExaminationPaperModel createExaminationPaper(ExaminationPaperModel model,String resType) {
    	ExaminationPaperModel rtModel = (ExaminationPaperModel)ndResourceService.create(resType, model,DbName.DEFAULT);
        return rtModel;
    }

    @Override
    public ExaminationPaperModel updateExaminationPaper(ExaminationPaperModel model,String resType) {
    	ExaminationPaperModel rtModel = (ExaminationPaperModel)ndResourceService.update(resType, model,DbName.DEFAULT);
        return rtModel;
    }

    @Override
    public ExaminationPaperModel patchExaminationPaper(ExaminationPaperModel model,String resType) {
        return (ExaminationPaperModel)ndResourceService.patch(resType, model, DbName.DEFAULT);
    }

}
