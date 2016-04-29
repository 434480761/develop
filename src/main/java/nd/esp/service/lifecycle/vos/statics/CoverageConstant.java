package nd.esp.service.lifecycle.vos.statics;

import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.utils.StringUtils;

/**
 * 公私有资源库相关常量
 * <p>Create Time: 2015年6月17日           </p>
 *
 * @author xiezy
 */
public class CoverageConstant {

    //覆盖范围黑名单
    private static final CoverageModel BLACKLIST_COVERATE_VALUE = new CoverageModel();

    static {

        BLACKLIST_COVERATE_VALUE.setTarget("workspace");
        BLACKLIST_COVERATE_VALUE.setTargetType("RSD");
        BLACKLIST_COVERATE_VALUE.setStrategy("ASSEMBLE");
        BLACKLIST_COVERATE_VALUE.setTargetTitle("ND资源拷贝");

    }
    
    /*
     * 所属,一个资源所属范围，比如属于个人的，属于某个机构的。只允许有一个宿主范围
     */
    public final static String STRATEGY_OWNER = Strategy.STRATEGY_OWNER.code;
    //机构,代表机构类型的覆盖
    public final static String TARGET_TYPE_ORG = TargetType.TARGET_TYPE_ORG.code;
    // Org/nd, 代表101资源
    public final static String ORG_CODE_ND = "nd";
    
    //公共库target
    public final static String TARGET_PUBLIC = "public";

     /*
      * 初始化模板,在编辑器中存储编辑器用到的初始化模板
      *
      * */
    public final static String STRATEGY_INITTAMPLATE = Strategy.STRATEGY_INITTAMPLATE.getCode();
    
    //智能出题覆盖范围
    public final static String INTELLI_KNOWLEDGE_COVERAGE = 
            TargetType.TARGET_TYPE_SERVICE.getCode() + "/intelli-knowledge/" + Strategy.STRATEGY_SUPPORT.getCode();

    /**
     * 判断资源操作类型是否在可选范围内
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     *
     * @param strategy 资源操作类型
     * @param flag 判断可读or可写    true:可读   false:可写
     */
    public static boolean isCoverageStrategy(String strategy,boolean flag) {
        if(flag){
            return Strategy.isCoverageStrategy4Read(strategy);
        }
        return Strategy.isCoverageStrategy4Write(strategy);
    }

    /**
     * 判断覆盖范围类型是否在可选范围内
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     *
     * @param targetType 覆盖范围类型
     * @param flag 判断可读or可写    true:可读   false:可写
     */
    public static boolean isCoverageTargetType(String targetType,boolean flag) {
        if(flag){
            return TargetType.isCoverageTargetType4Read(targetType);
        }
        return TargetType.isCoverageTargetType4Write(targetType);
    }

    /*
     *   通用查询覆盖范围黑名单
     *   @see nd.esp.service.lifecycle.controllers.v06.DuplicateControllerV06#copyResources
     */ 
    public static CoverageModel getBlacklistCoverateValue() {
        return BLACKLIST_COVERATE_VALUE;
    }


    /**
     * target_type：覆盖范围的类型
     */
    public enum TargetType {
        /**
         * 机构,代表机构类型的覆盖
         */
        TARGET_TYPE_ORG("Org",true,true),
        /**
         * 角色,覆盖到某类角色
         */
        TARGET_TYPE_ROLE("Role",true,true),
        /**
         * 个人,覆盖到某个人
         */
        TARGET_TYPE_USER("User",true,true),
        /**
         * 时间,描述当前资源覆盖的时间范围，比如：二战时期
         */
        TARGET_TYPE_TIME("Time",true,true),
        /**
         * 地理空间,描述当前资源覆盖的空间范围，比如：长江中下游
         */
        TARGET_TYPE_SPACE("Space",true,true),
        /**
         * 群组,覆盖到某个群组
         */
        TARGET_TYPE_GROUP("Group",true,true),
        /**
         * 工作空间,覆盖到某个资源的工作空间,其标识的字符串为Workspace
         */
        TARGET_TYPE_RSD("RSD",true,true),
        /**
         * 用于测试,用于测试数据的隔离
         */
        TARGET_TYPE_DEBUG("Debug",true,true),
        /**
         * 对于提供服务的标识
         */
        TARGET_TYPE_SERVICE("Service",true,false),
        /**
         * 公共库
         */
        TARGET_TYPE_PB("PB",true,true);
        
        //属性
        String code;
        boolean readable;
        boolean writable;
        
        TargetType(String code,boolean readable,boolean writable) {
            this.code = code;
            this.readable = readable;
            this.writable = writable;
        }

        public String getCode() {
            return code;
        }
        public boolean isReadable() {
            return readable;
        }
        public boolean isWritable() {
            return writable;
        }

        /**
         * 判断覆盖范围类型是否在可选范围内(可读)
         * <p>Create Time: 2015年10月26日   </p>
         * <p>Create author: liuwx   </p>
         *
         * @param targetType 覆盖范围类型
         */
        public static boolean isCoverageTargetType4Read(String targetType) {

            if (StringUtils.hasText(targetType)) {
                for(TargetType type:TargetType.values()){
                    if(type.getCode().equals(targetType)){
                        if(type.isReadable()){
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        /**
         * 判断覆盖范围类型是否在可选范围内(可写)
         * <p>Create Time: 2015年10月26日   </p>
         * <p>Create author: xiezy   </p>
         *
         * @param targetType 覆盖范围类型
         */
        public static boolean isCoverageTargetType4Write(String targetType) {

            if (StringUtils.hasText(targetType)) {
                for(TargetType type:TargetType.values()){
                    if(type.getCode().equals(targetType)){
                        if(type.isWritable()){
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * strategy:资源操作类型
     */
    public enum Strategy {
        /**
         * 分享,只此资源分享给某个个人或者机构
         */
        STRATEGY_SHAREING("SHAREING",true,true),
        /**
         * 上报,下级上报给上级机构的资源
         */
        STRATEGY_REPORTING("REPORTING",true,true),
        /**
         * 所属,一个资源所属范围，比如属于个人的，属于某个机构的。只允许有一个宿主范围
         */
        STRATEGY_OWNER("OWNER",true,true),
        /**
         * 包含,在宿主资源域中资源，可以被包含在当前资源中。此类资源只能在当前资源被激活的情况下可用
         */
        STRATEGY_ASSEMBLE("ASSEMBLE",true,true),
        /**
         * 收藏,用户喜欢的资源可以进行收藏进自己的收藏夹
         */
        STRATEGY_FAVORITE("FAVORITE",true,true),
        /**
         * 初始化模板,在编辑器中存储编辑器用到的初始化模板
         */
        STRATEGY_INITTAMPLATE("INITTAMPLATE",true,true),
        /**
         * 测试,测试数据的隔离
         */
        STRATEGY_TEST("TEST",true,true),
        /**
         * 提供服务支持
         */
        STRATEGY_SUPPORT("SUPPORT",true,false);

        //属性
        String code;
        boolean readable;
        boolean writable;
        
        Strategy(String code,boolean readable,boolean writable) {
            this.code = code;
            this.readable = readable;
            this.writable = writable;
        }

        public String getCode() {
            return code;
        }
        public boolean isReadable() {
            return readable;
        }
        public boolean isWritable() {
            return writable;
        }
        
        /**
         * 判断资源操作类型是否在可选范围内(可读)
         * <p>Create Time: 2015年10月26日   </p>
         * <p>Create author: liuwx   </p>
         *
         * @param strategy 资源操作类型
         */
        public static boolean isCoverageStrategy4Read(String strategy) {

            if (StringUtils.hasText(strategy)) {
                for(Strategy type:Strategy.values()){
                    if(type.getCode().equals(strategy)){
                        if(type.isReadable()){
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        /**
         * 判断资源操作类型是否在可选范围内(可写)
         * <p>Create Time: 2015年10月26日   </p>
         * <p>Create author: xiezy   </p>
         *
         * @param strategy 资源操作类型
         */
        public static boolean isCoverageStrategy4Write(String strategy) {

            if (StringUtils.hasText(strategy)) {
                for(Strategy type:Strategy.values()){
                    if(type.getCode().equals(strategy)){
                        if(type.isWritable()){
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
    
//    public static void main(String[] args) {
//        String STRATEGY_SHAREING = "SHAREING";
//        Assert.assertTrue(isCoverageStrategy(STRATEGY_SHAREING));
//        String STRATEGY_REPORTING = "REPORTING";
//        Assert.assertTrue(isCoverageStrategy(STRATEGY_REPORTING));
//        String STRATEGY_OWNER = "OWNER";
//        Assert.assertTrue(isCoverageStrategy(STRATEGY_OWNER));
//        String STRATEGY_ASSEMBLE = "ASSEMBLE";
//        Assert.assertTrue(isCoverageStrategy(STRATEGY_ASSEMBLE));
//        String STRATEGY_FAVORITE = "FAVORITE";
//        Assert.assertTrue(isCoverageStrategy(STRATEGY_FAVORITE));
//        String STRATEGY_INITTAMPLATE = "INITTAMPLATE";
//        Assert.assertTrue(isCoverageStrategy(STRATEGY_INITTAMPLATE));
//        String STRATEGY_TEST = "TEST";
//        Assert.assertTrue(isCoverageStrategy(STRATEGY_TEST));
//        //反向测试
//        String STRATEGY_TEST_1 = "TEST1";
//        Assert.assertFalse(isCoverageStrategy(STRATEGY_TEST_1));
//
//
//        String  TARGET_TYPE_ORG = "Org";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_ORG));
//        String TARGET_TYPE_ROLE = "Role";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_ROLE));
//        String TARGET_TYPE_USER = "User";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_USER));
//        String TARGET_TYPE_TIME = "Time";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_TIME));
//        String TARGET_TYPE_SPACE = "Space";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_SPACE));
//        String TARGET_TYPE_GROUP = "Group";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_GROUP));
//        String TARGET_TYPE_RSD = "RSD";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_RSD));
//        String TARGET_TYPE_DEBUG = "Debug";
//        Assert.assertTrue(isCoverageTargetType(TARGET_TYPE_DEBUG));
//        String TARGET_TYPE_DEBUG_1 = "Debug1";
//        Assert.assertFalse(isCoverageTargetType(TARGET_TYPE_DEBUG_1));
//    }
}
