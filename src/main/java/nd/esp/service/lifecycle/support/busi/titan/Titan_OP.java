package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.utils.CollectionUtils;

/**
 * titan 相关操作符
 * 
 * @author linsm
 */
public enum Titan_OP {

    // FIXME eq, in 对于同个字段不能共存
    eq {
        public String opToTitanString() {
            return TitanKeyWords.within.toString();
        }
    },
    in {
        public String opToTitanString() {
            return TitanKeyWords.within.toString();
        }
    },
    ne {
        public String opToTitanString() {
            return TitanKeyWords.without.toString();
        }
    },

    lt {
        public String opToTitanString() {
            return TitanKeyWords.lt.toString();
        }
    },
    le {
        public String opToTitanString() {
            return TitanKeyWords.lte.toString();
        }
    },
    gt {
        public String opToTitanString() {
            return TitanKeyWords.gt.toString();
        }
    },
    ge {
        public String opToTitanString() {
            return TitanKeyWords.gte.toString();
        }
    },
    like {
        public String opToTitanString() {
            return TitanKeyWords.textRegex.toString();
        }
    },
	fulltextlike {
		public String opToTitanString() {
			return TitanKeyWords.textContains.toString();
		}
	}
    ;

    private final String matchAllCharacters = ".*";
    private final String or = "|";
    private final String matchBeforeCharacterNoneOrMany = "*";
    // private static final Logger LOG =
    // LoggerFactory.getLogger(Titan_OP.class);
    /**
     * 放置字符串值与枚举值的对应关系
     */
    private static Map<String, Titan_OP> map = new HashMap<String, Titan_OP>();
    // 初始化值
    static {
        for (Titan_OP es_OP : Titan_OP.values()) {
            map.put(es_OP.toString(), es_OP);
        }
    }

    // 通过字符串值返回枚举值
    public static Titan_OP fromString(String opString) {
        return map.get(opString);
    }

    // 操作符产生脚本
    public abstract String opToTitanString();

    /**
     * 产生脚本块，并修改参数值
     * 
     * @param field
     *            字段名
     * @param values
     *            值
     * @param scriptParamMap
     *            整个脚本的参数值
     * @return 返回脚本块，并修改脚本参数
     * @author linsm
     */
    public String generateScipt(String field, List<Object> values,
            Map<String, Object> scriptParamMap) {
        if (CollectionUtils.isEmpty(values)) {
            return "";
        }
        StringBuffer scriptBuffer = new StringBuffer();
        scriptBuffer.append(".").append(TitanKeyWords.has.toString())
                .append("('").append(field).append("',");
        if (values.size() == 1 && this == eq) {
            String valueKey = TitanUtils.generateKey(scriptParamMap, field);
            scriptBuffer.append(valueKey).append(")");
            scriptParamMap.put(valueKey, values.get(0));
		} else if (this.equals(fulltextlike)) {
			String valueKey = TitanUtils.generateKey(scriptParamMap, field);
			scriptBuffer.append(opToTitanString()).append("(").append(valueKey)
					.append("))");
			scriptParamMap.put(valueKey, values.get(0));
		}
        else if (this.equals(like)) {
            scriptBuffer.append(likeOperation(field, values, scriptParamMap));
        }
        else if (isltOrLteOrGtOrGteOperation()) {
            scriptBuffer.append(ltOrLteOrGtOrGteOperation(field, scriptParamMap, values));
        }
        else if(this.equals(ne)) {
            // 字段为空时处理
            scriptBuffer.setLength(0);
            scriptBuffer.append(".or(hasNot('").append(field).append("'),has('").append(field).append("',");
            scriptBuffer.append(opToTitanString()).append("(");
            for (Object value : values) {
                String valueKey = TitanUtils.generateKey(scriptParamMap, field);
                scriptBuffer.append(valueKey).append(",");
                scriptParamMap.put(valueKey, value);
            }
            // remove the last ","
            scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);

            scriptBuffer.append(")))");
        }else {
            scriptBuffer.append(opToTitanString()).append("(");
            for (Object value : values) {
                String valueKey = TitanUtils.generateKey(scriptParamMap, field);
                scriptBuffer.append(valueKey).append(",");
                scriptParamMap.put(valueKey, value);
            }
            // remove the last ","
            scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);

            scriptBuffer.append("))");
        }

        return scriptBuffer.toString();
    }

    private boolean isltOrLteOrGtOrGteOperation() {
        return this.equals(lt) || this.equals(le) || this.equals(gt) || this.equals(ge);
    }

    private StringBuilder ltOrLteOrGtOrGteOperation(String field, Map<String, Object> scriptParamMap,
            List<Object> values) {
        StringBuilder scriptBuffer = new StringBuilder().append(opToTitanString()).append("(");
        String valueKey = TitanUtils.generateKey(scriptParamMap, field);
        scriptBuffer.append(valueKey).append(",");
        scriptParamMap.put(valueKey, produceOneValidTime(values));
        // remove the last ","
        scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);

        scriptBuffer.append("))");
        return scriptBuffer;
    }

	/**
	 * 相同字段，相同操作符，or
	 * 
	 * @param values
	 * @return
	 */
	private Long produceOneValidTime(List<Object> values) {
		List<Long> times = new ArrayList<Long>();
		for (Object value : values) {
			times.add(Long.valueOf(value.toString()));
		}

		switch (this) {
		case gt:
		case ge:
			return Collections.min(times);
		case lt:
		case le:
			return Collections.max(times);
		default:
			return 0L;
		}
	}

	private StringBuilder likeOperation(String field, List<Object> values, Map<String, Object> scriptParamMap) {

        StringBuilder scriptBuffer = new StringBuilder().append(opToTitanString()).append("(");
        String valueKey = TitanUtils.generateKey(scriptParamMap, field);
        scriptBuffer.append(valueKey);
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            String formatDollarCharacters = String.valueOf(values.get(i)).replace("$", "\\$");
            if (formatDollarCharacters.contains(matchBeforeCharacterNoneOrMany)) {
                regex.append(formatDollarCharacters.replace(matchBeforeCharacterNoneOrMany, matchAllCharacters))
                        .append(or);
            } else {
                regex.append(matchAllCharacters).append(formatDollarCharacters)
                        .append(matchAllCharacters).append(or);
            }
            if (i == values.size() - 1) {
                regex.deleteCharAt(regex.length() - 1);
                // new StringBuilder("'").append(regex).append("'");
            }
        }
        scriptParamMap.put(valueKey, regex.toString());
        scriptBuffer.append("))");
        return scriptBuffer;
    }

    /******************************* TEST ***************************/
    public static void main(String[] args) {
        Titan_OP op = Titan_OP.le;
        Map<String, Object> scriptParamMap = new HashMap<String, Object>();
        List<Object> values = new ArrayList<Object>();
        values.add("1111");
        values.add("2222");
        values.add("3333");
        System.out.println(op.generateScipt("identifier", values,
                scriptParamMap));
        System.out.println(scriptParamMap);
    }

}
