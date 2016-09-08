package nd.esp.service.lifecycle.repository.model;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 子教学目标
 * 创建人: yanguanyu(290536)
 * 创建时间:2016-07-18
 */

@Entity
@Table(name = "sub_instruction")
public class SubInstruction extends Education {

    @Override
    public IndexSourceType getIndexType() {
        setPrimaryCategory(IndexSourceType.SubInstructionType.getName());
        return IndexSourceType.SubInstructionType;
    }
}
