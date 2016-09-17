package nd.esp.service.lifecycle.services.examinationpapers.v06.impl;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.ExaminationPaperModel;
import nd.esp.service.lifecycle.services.examinationpapers.v06.ExaminationPaperServiceV06;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

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
    public ExaminationPaperModel createExaminationPaper(ExaminationPaperModel model) {
    	ExaminationPaperModel rtModel = (ExaminationPaperModel)ndResourceService.create(ResourceNdCode.examinationpapers.toString(), model,DbName.DEFAULT);
        return rtModel;
    }

    @Override
    public ExaminationPaperModel updateExaminationPaper(ExaminationPaperModel model) {
    	ExaminationPaperModel rtModel = (ExaminationPaperModel)ndResourceService.update(ResourceNdCode.examinationpapers.toString(), model,DbName.DEFAULT);
        return rtModel;
    }

    @Override
    public ExaminationPaperModel patchExaminationPaper(ExaminationPaperModel model) {
        return (ExaminationPaperModel)ndResourceService.patch(ResourceNdCode.examinationpapers.toString(), model, DbName.DEFAULT);
    }

}
