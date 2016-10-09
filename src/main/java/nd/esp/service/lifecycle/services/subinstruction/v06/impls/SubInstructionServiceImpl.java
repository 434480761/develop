package nd.esp.service.lifecycle.services.subinstruction.v06.impls;

import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.models.v06.SubInstructionModel;
import nd.esp.service.lifecycle.repository.v02.ResourceRelationApiService;
import nd.esp.service.lifecycle.services.coverages.v06.CoverageService;
import nd.esp.service.lifecycle.services.educationrelation.v06.EducationRelationServiceV06;
import nd.esp.service.lifecycle.services.subinstruction.v06.SubInstructionService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.ChapterService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.TeachingMaterialServiceV06;
import nd.esp.service.lifecycle.support.annotation.TitanTransaction;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字母表类型业务实现类
 *
 * @author yanguanyu(290536)
 */
@Service("subInstructionServiceV06")
@Transactional
public class SubInstructionServiceImpl implements SubInstructionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubInstructionServiceImpl.class);

    @Autowired
    private NDResourceService ndResourceService;

    @Autowired
    @Qualifier("educationRelationServiceV06")
    private EducationRelationServiceV06 educationRelationService;

    @Autowired
    @Qualifier(value = "coverageServiceImpl")
    private CoverageService coverageService;

    @Autowired
    private JdbcTemplate jt;

    @Autowired
    private ResourceRelationApiService resourceRelationApiService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private TeachingMaterialServiceV06 teachingMaterialService;


    @Override
    @TitanTransaction
    public SubInstructionModel createSubInstruction(SubInstructionModel subInstructionModel) {
        // 调用通用创建接口
        subInstructionModel.setTechInfoList(null);
        subInstructionModel = (SubInstructionModel) ndResourceService.create(ResourceNdCode.subInstruction.toString(),
                subInstructionModel);
        subInstructionModel.setPreview(null);
        subInstructionModel.setEducationInfo(null);

        return subInstructionModel;
    }

    @Override
    @TitanTransaction
    public SubInstructionModel updateSubInstruction(SubInstructionModel subInstructionModel) {
        SubInstructionModel rtSubInstructionModel = (SubInstructionModel)ndResourceService.update(ResourceNdCode.subInstruction.toString(), subInstructionModel);
        return rtSubInstructionModel;
    }

}
