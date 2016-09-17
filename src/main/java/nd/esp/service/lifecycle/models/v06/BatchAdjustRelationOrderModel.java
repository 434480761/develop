package nd.esp.service.lifecycle.models.v06;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 用于批量修改资源关系顺序的Model
 * <p>Create Time: 2015年5月21日           </p>
 * @author xiezy
 */
public class BatchAdjustRelationOrderModel {
    /**
     * 需要移动的目标对象
     */
    @NotBlank(message="{batchAdjustRelationOrderModel.target.notBlank.validmsg}")
    private String target;
    /**
     * 移动目的地靶心对象
     */
    @NotBlank(message="{batchAdjustRelationOrderModel.destination.notBlank.validmsg}")
    private String destination;
    /**
     * 相邻对象的id，如果在第一个和最后一个的时候，不存在相邻对象，传入为none。
     */
    @NotBlank(message="{batchAdjustRelationOrderModel.adjoin.notBlank.validmsg}")
    private String adjoin;
    /**
     * 移动的方向标识，first是移动到第一个位置，last是将这个关系增加到列表的最后，middle是将目标增加到destination和adjoin中间。
     */
    @NotBlank(message="{batchAdjustRelationOrderModel.at.notBlank.validmsg}")
    private String at;
    
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public String getAdjoin() {
        return adjoin;
    }
    public void setAdjoin(String adjoin) {
        this.adjoin = adjoin;
    }
    public String getAt() {
        return at;
    }
    public void setAt(String at) {
        this.at = at;
    }
}
