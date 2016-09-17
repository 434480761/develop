package nd.esp.service.lifecycle.support;

/**
 * @author johnny
 * @version 1.0
 * @created 07-4月-2015 16:09:14
 */
public enum ResRelationType {
    PART,
    VERSION,
    /**
     * A 与B 有相同的格式，B 先于A
     * B 与A 有相同的格式，A 先于B
     */
    BEFORE,
    /**
     * A 参考引用了B
     * B 参考引用了A
     */
    QUOTE,
    /**
     * A 基于B
     * B 基于A
     */
    BASEON,
    /**
     * A 需要B
     * B 需要A
     */
    REQUIRED,
    /**
     * 关联关系
     */
    ASSOCIATE
}
