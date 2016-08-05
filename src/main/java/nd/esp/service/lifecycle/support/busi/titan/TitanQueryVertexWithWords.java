package nd.esp.service.lifecycle.support.busi.titan;

import java.util.*;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

public class TitanQueryVertexWithWords extends TitanQueryVertex {

    private boolean isFilter = false;
    private String words;
    private Map<String, Object> searchCodesConditions;
    private Map<String, Object> neLikesearchCodesConditions;
    private Map<String, Object> searchPathsConditions;


    public void setIsFilter(String resType) {
        if (resType != null) {
            this.isFilter = ResourceNdCode.lessons.toString().equals(resType) || ResourceNdCode.instructionalobjectives.toString().equals(resType);
        }
    }



    public void setSearchPathsConditions(Map<String, Object> searchPathsConditions) {
        this.searchPathsConditions = searchPathsConditions;
    }

    public void setSearchCodesConditions(Map<String, Object> searchCodesConditions) {
        this.searchCodesConditions = searchCodesConditions;
    }

    public void setNeLikesearchCodesConditions(Map<String, Object> neLikesearchCodesConditions) {
        this.neLikesearchCodesConditions = neLikesearchCodesConditions;
    }

    // FIXME 暂时放在一起
    public void setWords(String words) {
//        String[] chunks = words.split("\\$");
//        if (!"null".equals(chunks[0])) {
//            words = chunks[0].trim();
//        }
//
//        //System.out.println(stringWords);
//
//        if (!"null".equals(chunks[1])) {
//            textWords = chunks[1].trim();
//        }
    	if(StringUtils.isNotEmpty(words)){
    		this.words = words.trim();
    	}
    	

        //System.out.println(textWords);
    }

    @Override
    public String generateScript(Map<String, Object> scriptParamMap) {

        StringBuffer scriptBuffer = new StringBuffer(
                super.generateScript(scriptParamMap));
        if (StringUtils.isNotEmpty(words)) {
            scriptBuffer.append(".").append(TitanKeyWords.or.toString())
                    .append("(");
            List<Object> values = new ArrayList<Object>();
            values.add(words);
            for (WordsCover wordsCover : WordsCover.values()) {
                // 过滤
                if (isFilter && wordsCover.equals(WordsCover.edu_description)) continue;
                // remove the "."
                scriptBuffer.append(Titan_OP.like.generateScipt(
                        wordsCover.toString(), values, scriptParamMap)
                        .substring(1));
                scriptBuffer.append(",");
            }
            scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
            scriptBuffer.append(")");
        }

//        if (StringUtils.isNotEmpty(textWords)) {
//            scriptBuffer.append(".").append(TitanKeyWords.or.toString())
//                    .append("(");
//            List<Object> values = new ArrayList<Object>();
//            values.add(textWords);
//            for (WordsCover wordsCover : WordsCover.values()) {
//
//                // remove the "."
//                scriptBuffer.append(Titan_OP.fulltextlike.generateScipt(
//                        wordsCover.toString(), values, scriptParamMap)
//                        .substring(1));
//                scriptBuffer.append(",");
//            }
//            scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
//            scriptBuffer.append(")");
//        }

        if (CollectionUtils.isNotEmpty(searchCodesConditions)) {
            scriptBuffer.append(".").append(TitanKeyWords.or.toString()).append("(");
            for (Map.Entry<String, Object> entry : searchCodesConditions.entrySet()) {
                String opt = entry.getKey();
                if (opt.equals(PropOperationConstant.OP_AND)) {
                    List<List<String>> andCodeList = (List) entry.getValue();
                    for (List<String> codes : andCodeList) {
                        List<Object> likeInAndCodeList = new ArrayList<>();
                        for (String code : codes) {// and contains like
                            if (code.contains("*")) {
                                likeInAndCodeList.add(code.trim());
                                scriptBuffer.append(Titan_OP.like.generateScipt("search_code", likeInAndCodeList, scriptParamMap).replaceFirst(".", "")).append(".");
                                likeInAndCodeList.clear();
                            } else {
                                scriptBuffer.append("has('search_code',");
                                String uniqueKey = TitanUtils.generateKey(scriptParamMap, "search_code");
                                scriptBuffer.append(uniqueKey);
                                scriptBuffer.append(").");
                                scriptParamMap.put(uniqueKey, code.trim());
                            }
                            /*if (likeInAndCodeList.size() > 0) {
                                scriptBuffer.append(Titan_OP.like.generateScipt("search_code", likeInAndCodeList, scriptParamMap).replaceFirst(".", "")).append(".");
                            }*/
                        }
                        scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
                        scriptBuffer.append(",");
                    }
                } else if (opt.equals(PropOperationConstant.OP_LIKE)) {
                    List<Object> likeCodeList = (List) entry.getValue();
                    if (likeCodeList.size() > 0) {
                        scriptBuffer.append(Titan_OP.like.generateScipt("search_code", likeCodeList, scriptParamMap).replaceFirst(".", "")).append(",");
                    }
                }/*else if (opt.equals(PropOperationConstant.OP_NE+PropOperationConstant.OP_LIKE)) {
                    neLikeCodeList = (List) entry.getValue();
                }*/ else {
                    List<String> inCodeList = (List) entry.getValue();
                    for (String code : inCodeList) {
                        scriptBuffer.append("has('search_code',");
                        String uniqueKey = TitanUtils.generateKey(scriptParamMap, "search_code");
                        scriptBuffer.append(uniqueKey);
                        scriptBuffer.append("),");
                        scriptParamMap.put(uniqueKey, code.trim());
                    }
                }
                //System.out.println( entry.getKey() + " : " + entry.getValue());
            }
            scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
            scriptBuffer.append(")");


        }

        // ne like
        if (CollectionUtils.isNotEmpty(neLikesearchCodesConditions)) {
            for (Map.Entry<String, Object> entry : neLikesearchCodesConditions.entrySet()) {
                String opt = entry.getKey();
                if (opt.equals(PropOperationConstant.OP_NE+PropOperationConstant.OP_LIKE)) {
                    List<Object>  neLikeCodeList = (List) entry.getValue();
                    scriptBuffer.append(".not(").append(Titan_OP.like.generateScipt("search_code", neLikeCodeList, scriptParamMap).replaceFirst(".", "")).append(")");
                }
            }

        }

        if (CollectionUtils.isNotEmpty(searchPathsConditions)) {
            scriptBuffer.append(".").append(TitanKeyWords.or.toString()).append("(");
            for (Map.Entry<String, Object> entry : searchPathsConditions.entrySet()) {
                String opt = entry.getKey();
                if (opt.equals(PropOperationConstant.OP_LIKE)) {
                    List<Object> likeCodeList = (List) entry.getValue();
                    if (likeCodeList.size() > 0) {
                        scriptBuffer.append(Titan_OP.like.generateScipt("search_path", likeCodeList, scriptParamMap).replaceFirst(".", "")).append(",");
                    }
                } else if (opt.equals(PropOperationConstant.OP_IN)) {
                    List<String> inCodeList = (List) entry.getValue();
                    if(inCodeList.size()>0) {
                        for (String code : inCodeList) {
                            scriptBuffer.append("has('search_path',");
                            String uniqueKey = TitanUtils.generateKey(scriptParamMap, "search_path");
                            scriptBuffer.append(uniqueKey);
                            scriptBuffer.append("),");
                            scriptParamMap.put(uniqueKey, code);
                        }
                    }

                }
            }
            scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
            scriptBuffer.append(")");

        }

        return scriptBuffer.toString();
    }

    public static enum WordsCover {
        title, description, keywords, tags, edu_description, cr_description
    }

    /***************************************
     * TEST
     **********************************/
    public static void main(String[] args) {

        testGenerateScript();

        // testWords();
    }

    static void testGenerateScript() {
        TitanQueryVertex titanQueryVertex = TitanQueryVertex
                .generateTestExample();
        TitanQueryVertexWithWords titanQueryVertexWithWords = new TitanQueryVertexWithWords();

        titanQueryVertexWithWords.setPropertiesMap(titanQueryVertex
                .getPropertiesMap());
        titanQueryVertexWithWords.setTitanDirection(titanQueryVertex
                .getTitanDirection());
        titanQueryVertexWithWords.setVertexLabel(titanQueryVertex
                .getVertexLabel());
        String stringWords = null;
        String fulltext = null;
        String words = stringWords + "$" + fulltext;
        System.out.println(words);
        titanQueryVertexWithWords.setWords(words);

        Map<String, Object> scriptParamMap = new HashMap<String, Object>();
        System.out.println(titanQueryVertexWithWords
                .generateScript(scriptParamMap));
        System.out.println(scriptParamMap);

    }

    static void testWords() {
        TitanQueryVertexWithWords testObjecTitanQueryVertexWithWords = new TitanQueryVertexWithWords();
        String stringWords = null;
        String fulltext = null;
        String words = stringWords + "$" + fulltext;
        System.out.println(words);
        testObjecTitanQueryVertexWithWords.setWords(words);

        stringWords = "stringWords";

        words = stringWords + "$" + fulltext;
        System.out.println(words);
        testObjecTitanQueryVertexWithWords.setWords(words);

        fulltext = "fulltext";

        words = stringWords + "$" + fulltext;
        System.out.println(words);
        testObjecTitanQueryVertexWithWords.setWords(words);
    }

}