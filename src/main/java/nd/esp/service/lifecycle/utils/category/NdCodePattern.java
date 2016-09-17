/* =============================================================
 * Created: [2015年7月29日] by linsm
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.utils.category;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

/**
 * 用于配置ndCode的规则
 *
 * @author linsm
 * @since
 */
public enum NdCodePattern {

    $O("\\$O[NA]{1}|\\$O[NA]{1}[0-9]{2}|\\$O[NA]{1}[0-9]{4}|\\$O[NA]{1}[0-9]{6}", 9, 3, "标识正规教育和非正规教育，正规教育使用N标识，非正规教育采用A标识") {
        void setUp(Map<Integer, Integer> map) {
            map.put(3, 2);
            map.put(5, 2);
            map.put(7, 2);
        }
    }, // 适用对象
    $S("\\$S[BHX]{1}|\\$S[BHX]{1}[0-9]{2}|\\$S[BHX]{1}[0-9]{4}", 7, 3, "用一位大学英文字母标识，其中“B”代表基础学科，“H”代表高等学科，“X”代表用户自定义扩展学科") {
        void setUp(Map<Integer, Integer> map) {
            map.put(3, 2);
            map.put(5, 2);
        }
    }, // 学科
    $E("\\$E|\\$E[0-9]{3}|\\$E[0-9]{6}", 8, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 3);
        }
    }, // 版本
    $R("\\$R[TSECAR]{1}|\\$R[TSECAR]{1}[0-9]{2}|\\$R[TSECAR]{1}[0-9]{4}", 7, 3, "用一位大写英文字母标识，其中“T”代表教学资源，“S”代表学习资源，“E”代表评测资源，“C”代表培训资源，“A”代表素材资源，“R”代表学科工具") {
        void setUp(Map<Integer, Integer> map) {
            map.put(3, 2);
            map.put(5, 2);
        }
    }, // 资源
    $F("\\$F|\\$F[0-9]{2}|\\$F[0-9]{6}", 8, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 4);
        }
    }, // 媒体
    OT("OT[KPACS]{1}|OT[KPACS]{1}[0-9]{2}|OT[KPACS]{1}[0-9]{4}", 7, 3, "用一位大写英文字母标识，其中“K”代表知识与技能，“P”代表过程与方法，“A”代表情感态度与价值观,C为自定义教学目标分类,S代表学科目标") {
        void setUp(Map<Integer, Integer> map) {
            map.put(3, 2);
            map.put(5, 2);
        }
    }, // 教学目标
    $C("\\$C|\\$C[0-9]{2}|\\$C[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
        }
    }, // 课时
    RF("RF|RF[0-9]{2}|RF[0-9]{5}", 7, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 3);
        }
    }, // 资源来源
    RR("RR|RR[0-9]{2}", 4, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
        }
    },// 资源关系
    TS("TS|TS[0-9]{2}|TS[0-9]{5}|TS[0-9]{8}|TS[0-9]{13}", 15, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 3);
            map.put(7, 3);
            map.put(10, 5);
        }
    }, //主题风格

    WS("WS|WS[0-9]{2}|WS[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
        }
    }, //词性

    AL("AL|AL[0-9]{3}", 5, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
        }
    }, //地区语种

    ZQ("ZQ|ZQ[0-9]{2}|ZQ[0-9]{5}|ZQ[0-9]{8}", 10, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 3);
            map.put(7, 3);
        }
    }, //折纸课程分类

    ZZ("ZZ|ZZ[0-9]{3}", 5, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
        }
    }, //造字法分类维度

    ZX("ZX|ZX[0-9]{3}", 5, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
        }
    }, //字形结构分类维度
    SD("SD|SD[0-9]{2}", 4, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
        }
    }, //声调的分类维度

    // 2016_02_03***************************************************************************
    TD("TD|TD[0-9]{2}|TD[0-9]{5}", 7, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 3);
        }
    }, // 3D技术领域组件的分类维度
    BC("BC|BC[0-9]{1}|BC[0-9]{3}|BC[0-9]{5}|BC[0-9]{8}|BC[0-9]{11}|BC[0-9]{14}|BC[0-9]{17}|BC[0-9]{20}", 22, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 1);
            map.put(3, 2);
            map.put(5, 2);
            map.put(7, 3);
            map.put(10, 3);
            map.put(13, 3);
            map.put(16, 3);
            map.put(19, 3);
        }
    }, // 生物分类维度
    HO("HO|HO[0-9]{2}|HO[0-9]{5}|HO[0-9]{8}|HO[0-9]{11}", 13, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 3);
            map.put(7, 3);
            map.put(10, 3);
        }
    }, // 人体器官的分类维度
    FM("FM|FM[0-9]{2}|FM[0-9]{5}|FM[0-9]{8}", 10, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 3);
            map.put(7, 3);
        }
    }, // 食品分类维度
    VC("VC|VC[0-9]{2}|VC[0-9]{7}", 9, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 5);
        }
    }, // 交通工具分类维度
    LS("LS|LS[0-9]{2}|LS[0-9]{7}", 9, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 5);
        }
    }, // 学习用品分类维度
    DU("DU|DU[0-9]{2}|DU[0-9]{4}|DU[0-9]{7}", 9, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
            map.put(6, 3);
        }
    }, // 生活用品分类维度
    SG("SG|SG[0-9]{3}|SG[0-9]{7}", 9, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 4);
        }
    }, // 体育用品分类维度
    $A("\\$A|\\$A[0-9]{1}|\\$A[0-9]{3}|\\$A[0-9]{5}|\\$A[0-9]{7}", 9, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 1);
            map.put(3, 2);
            map.put(5, 2);
            map.put(7, 2);
        }
    }, // 武器分类维度
    FU("FU|FU[0-9]{2}|FU[0-9]{6}", 8, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 4);
        }
    }, // 家具分类维度
    BD("BD|BD[0-9]{1}|BD[0-9]{3}|BD[0-9]{6}|BD[0-9]{9}|BD[0-9]{12}", 14, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 1);
            map.put(3, 2);
            map.put(5, 3);
            map.put(8, 3);
            map.put(11, 3);
        }
    }, // 建筑分类维度

    NL("NL|NL[0-9]{2}|NL[0-9]{6}", 8, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 4);
        }
    }, // 自然景观分类维度
    RL("RL|RL[0-9]{3}|RL[0-9]{6}", 8, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 3);
        }
    }, // 自然景观分类维度
    // ***************************************************************************2016_02_03


    UK("UK|UK[0-9]{3}|UK[0-9]{7}|UK[0-9]{11}|UK[0-9]{15}", 17, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 4);
            map.put(9, 4);
            map.put(13, 4);
        }
    }, //未知的分类维度

    TC("TC|TC[0-9]{3}", 5, 2, ""){
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
        }
    },//临时分类维度
    LD("LD|LD[0-9]{1}|LD[0-9]{3}", 5, 2, ""){
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 1);
            map.put(3, 2);
        }
    },//LOD等级分类标识
    // 用于测试的分类维度
    WX("WX|WX[0-9]{2}|WX[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
        }
    },
    XX("XX|XX[0-9]{2}|XX[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
        }
    },
    XW("XW|XW[0-9]{2}|XW[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
        }
    },
    KC("KC|KC[0-9]{3}", 5, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
        }
    },
    KP("KP|KP[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 4);
        }
    },
    WW("WW|WW[0-9]{2}|WW[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
        }
    },
    PC("PC|PC[0-9]{2}|PC[0-9]{4}", 6, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
        }
    },
    /**
     * 提供商品牌的分类，由3部分，10位标识码组成。
		(1)PR (2)00 (3)000000
		第一层级：分类类别，用两位大写的英文字母标识，语的分类使用PR标识。
		第二层级：一级类目代码，用2位阿拉伯数字标识。
		第三层级：二级类目代码，用6位阿拉伯数字标识。
     */
    PR("PR|PR[0-9]{2}|PR[0-9]{8}", 10, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 6);
        }
    },
    /**
     * 写实风格的分类，由8部分，23位标识码组成。
		(1)RS (2)000 (3)000 (4)000 (5)000 (6)000 (7)000 (8)000
		第一层级：分类类别，用两位大写的英文字母标识，语的分类使用RS标识。
		第二层级：一级类目代码，用3位阿拉伯数字标识。
		第三层级：二级类目代码，用3位阿拉伯数字标识。
		第四层级：三级类目代码，用3位阿拉伯数字标识。
		第五层级：四级类目代码，用3位阿拉伯数字标识。
		第六层级：五级类目代码，用3位阿拉伯数字标识。
		第七层级：六级类目代码，用3位阿拉伯数字标识。
		第八层级：七级类目代码，用3位阿拉伯数字标识。
     */
    RS("RS|RS[0-9]{3}|RS[0-9]{6}|RS[0-9]{9}|RS[0-9]{12}|RS[0-9]{15}|RS[0-9]{18}|RS[0-9]{21}", 23, 2, "") {
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 3);
            map.put(8, 3);
            map.put(11,3);
            map.put(14,3);
            map.put(17,3);
            map.put(20,3);
        }
    },

    WT("WT|WT[0-9]{2}|WT[0-9]{5}", 7, 2 , ""){
        @Override
        void setUp(Map<Integer, Integer> map) {
            map.put(2,2);
            map.put(4,3);
        }
    },
    PT("PT|PT[0-9]{2}|PT[0-9]{5}", 7, 2 , ""){
        @Override
        void setUp(Map<Integer, Integer> map) {
            map.put(2,2);
            map.put(4,3);
        }
    },

    /**
     * VR资源管理类型维度，由11部分，32位标识码组成。
     (1)$V (2)000 (3)000 (4)000 (5)000 (6)000 (7)000 (8)000 (9)000 (10)000 (11)000
     第一层级：分类类别，用两位大写的英文字母标识，语的分类使用$V标识。
     第二层级：一级类目代码，用3位阿拉伯数字标识。
     第三层级：二级类目代码，用3位阿拉伯数字标识。
     第四层级：三级类目代码，用3位阿拉伯数字标识。
     第五层级：四级类目代码，用3位阿拉伯数字标识。
     第六层级：五级类目代码，用3位阿拉伯数字标识。
     第七层级：六级类目代码，用3位阿拉伯数字标识。
     第八层级：七级类目代码，用3位阿拉伯数字标识。
     第九层级：八级类目代码，用3位阿拉伯数字标识。
     第十层级：九级类目代码，用3位阿拉伯数字标识。
     第十一层级：十级类目代码，用3位阿拉伯数字标识。
     * */
    $V("\\$V|\\$V[0-9]{3}|\\$V[0-9]{6}|\\$V[0-9]{9}|\\$V[0-9]{12}|\\$V[0-9]{15}|\\$V[0-9]{18}|\\$V[0-9]{21}|\\$V[0-9]{24}|\\$V[0-9]{27}|\\$V[0-9]{30}", 32,2 ,""){
        @Override
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 3);
            map.put(8, 3);
            map.put(11,3);
            map.put(14,3);
            map.put(17,3);
            map.put(20,3);
            map.put(23,3);
            map.put(26,3);
            map.put(29,3);
        }
    },
    EE("EE|EE[0-9]{2}|EE[0-9]{4}|EE[0-9]{6}|EE[0-9]{8}", 10,2 ,""){
        @Override
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 2);
            map.put(4, 2);
            map.put(6, 2);
            map.put(8, 2);
        }
    },
    VA("VA|VA[0-9]{3}|VA[0-9]{6}|VA[0-9]{9}|VA[0-9]{12}", 14,2 ,""){
        @Override
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 3);
            map.put(8, 3);
            map.put(11, 3);
        }
    },
    CR("CR|CR[0-9]{3}|CR[0-9]{6}", 8,2 ,""){
        @Override
        void setUp(Map<Integer, Integer> map) {
            map.put(2, 3);
            map.put(5, 3);
        }
    };

    // 分类维度ndCode的长度(现在所有的分类维度编码长度都是2)
    public static final int CATEGORY_LENGTH = 2;
    // 辅助根据前缀分类维度code来获取到对应对象
    public static Map<String, NdCodePattern> StringToType = new HashMap<String, NdCodePattern>();
    static {
        for (NdCodePattern type : NdCodePattern.values()) {
            StringToType.put(type.toString(), type);
        }
    }

    // 维度数据匹配模式，包含各个层级
    private String pattern;
    // 各个层级与可扩展位数的对应关系
    private Map<Integer, Integer> prefixLengthToExtenedDigitMap = new HashMap<Integer, Integer>();
    // 维度数据总的位数
    private int length;
    // 最小可扩展位数（若小于此位数，则会带有语义）
    private int minPrefixLength;
    // 带有语义异常
    private String message;

    public static final String CATEGORYDATA_TOP_NODE_PARENT = "ROOT";

    // 配置各个分类维度各个粒度的扩展位数
    abstract void setUp(Map<Integer, Integer> map);

    /**
     * @param pattern  			正则表达式
     * @param length			总长度
     * @param minPrefixLength	第一层级的长度
     * @param message
     */
    private NdCodePattern(String pattern, int length, int minPrefixLength, String message) {
        setUp(prefixLengthToExtenedDigitMap);
        this.pattern = pattern;
        this.length = length;
        this.minPrefixLength = minPrefixLength;
        this.message = message;
    }

    /**
     * 判断是否合乎（ndCode前缀，按层级来划分）(包含它本身，全长的ndCode)
     *
     * @param prefix
     * @return
     * @since
     */
    public boolean isValidNdCodePrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return false;
        }

        return Pattern.matches(pattern, prefix);
    }

    public int getLength() {
        return length;
    }

    public int getMinPrefixLength() {
        return minPrefixLength;
    }

    public String getMessage() {
        return message;
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * 若已能过模式匹配，可以保证不为空
     *
     * @param prefixLength
     * @return
     * @since
     */
    public int getExtendDigit(int prefixLength) {
        Integer value = prefixLengthToExtenedDigitMap.get(prefixLength);
        if(value == null){
            return 0;
        }else{
            return value;
        }
    }

    /**
     * 通过名字来获取对应的对象
     *
     * @param name
     * @return
     * @since
     */
    public static NdCodePattern fromString(String name) {
        return StringToType.get(name);
    }

    /**
     * @param parentNdCode 父结点ndCode 或者是 相应分类维度ndCode  非ROOT
     * @return  某个层次（粒度）的前缀
     * @since
     */
    public String getPrefix(String parentNdCode) {
        String prefix = StringUtils.stripEnd(parentNdCode, "0"); // 去除最后的字符0

        int prefixLength = prefix.length();
        if (prefixLength < getMinPrefixLength()) {
            // 抛出具体语义的信息
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          getMessage());
        }
        while (prefixLength <= getLength()) {
            if (isValidNdCodePrefix(prefix)) {
                break; // 找到前缀
            } else {
                prefix += "0";// 补0
                prefixLength++;
            }

        }

        int extendDigit = getExtendDigit(prefixLength);
        if (extendDigit == 0) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          "已经是最细粒度的编码（不再可自动扩展）");
        }
        return prefix;
    }

    /**
     * @param parentNdCode 父结点ndCode  非ROOT
     * @param ndCode 当前结点ndCode
     * @return
     * @since
     */
    public Integer getOrderNum(String parentNdCode, String ndCode) {
        String prefix = getPrefix(parentNdCode);
        int length = getExtendDigit(prefix.length());
        return Integer.valueOf(ndCode.substring(prefix.length(), prefix.length() + length));
    }

    /**
     * 校验ndCode的关系， （分类维度、父结点ndCode、当前结点ndCode）
     *
     * @param categoryNdCode
     * @param parentNdCode 可能是ROOT
     * @param currentNdCode
     * @since
     */
    public static void checkNdCodeRelation(String categoryNdCode, String parentNdCode, String currentNdCode) {
        if (StringUtils.isEmpty(categoryNdCode) || StringUtils.isEmpty(parentNdCode)
                || StringUtils.isEmpty(currentNdCode)) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          "分类维度编码、维度数据的编码:nd_code全部都不能为空字符串");
        }
        // 分类维度是否已备案
        NdCodePattern pattern = fromString(categoryNdCode);
        if (pattern == null) {
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          "分类维度nd_code="+categoryNdCode+" 还没有备案，有需要请与LC沟通");
        }
        if (!pattern.isValidNdCodePrefix(currentNdCode) || currentNdCode.length() != pattern.getLength()
                || !currentNdCode.substring(0, CATEGORY_LENGTH).equals(categoryNdCode)) {



            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                          "nd_code="+currentNdCode+"编码不符合规范: " + pattern.getPattern());
        }

        String currentPrefix = StringUtils.stripEnd(currentNdCode, "0");
        // 父结点为根
        if (parentNdCode.equals(CATEGORYDATA_TOP_NODE_PARENT)) {
            // 不存在有语义的层级
            if (pattern.getMinPrefixLength() == CATEGORY_LENGTH) {
                if (currentPrefix.length() > CATEGORY_LENGTH + pattern.getExtendDigit(CATEGORY_LENGTH)) {
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                                  "nd_code="+currentNdCode+" 编码不符合规范: 层级不对");
                }

            } else {
                if (currentPrefix.length() != pattern.getMinPrefixLength()) {
                    // currentNdcode不是第一层
                    throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                  LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                                  "nd_code="+currentNdCode+" 编码不符合规范: 层级不对");
                }
            }
        } else {
            // 所有的ndCode都是正常的编码
            if (parentNdCode.length() != pattern.getLength()
                    || !parentNdCode.substring(0, CATEGORY_LENGTH).equals(categoryNdCode)) {
                // 父结点ndCode长度不对或者是与分类维度编码不一致
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                              "nd_code="+parentNdCode+" 编码不符合规范: " + pattern.getPattern());
            }

            // 父与子间的关系
            // String parentPrefix = StringUtils.stripEnd(parentNdCode, "0");
            String parentPrefix = pattern.getPrefix(parentNdCode);
            int extendDigit = pattern.getExtendDigit(parentPrefix.length());
            if (currentPrefix.length() > parentPrefix.length() + extendDigit) {
                // 层级不对
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                              "nd_code:"+currentNdCode+","+parentNdCode+" 编码不符合规范: 层级不对");
            }

            if (!currentPrefix.contains(parentPrefix) || parentPrefix.contains(currentPrefix)) {
                // 父子编码不一致
                throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              LifeCircleErrorMessageMapper.CheckNdCodeRegex.getCode(),
                                              "nd_code:"+currentNdCode+","+parentNdCode+" 编码不符合规范: 父子编码不一致");
            }
        }
    }

}
