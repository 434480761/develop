package nd.esp.service.lifecycle.support.busi.elasticsearch;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.support.busi.titan.TitanKeyWords;
import nd.esp.service.lifecycle.support.enums.ES_OP;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * ******************************************
 * <p/>
 * Copyright 2016
 * NetDragon All rights reserved
 * <p/>
 * *****************************************
 * <p/>
 * *** Company ***
 * NetDragon
 * <p/>
 * *****************************************
 * <p/>
 * *** Team ***
 * <p/>
 * <p/>
 * *****************************************
 *
 * @author gsw(806801)
 * @version V1.0
 * @Title EsIndexQueryForTitanSearch
 * @Package nd.esp.service.lifecycle.support.busi.elasticsearch
 * <p/>
 * *****************************************
 * @Description
 * @date 2016/7/15
 */
public class EsIndexQueryForTitanSearch {
    private String index="mixed_ndresource";
    private String words;
    public void setIndex(String index) {
        this.index = index;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String generateScript() {
        StringBuffer baseQuery=new StringBuffer("List<String> ids = new ArrayList<String>();graph.indexQuery(\"").append(this.index).append("\",\"");
        baseQuery.append(dealWithWords(this.words));
        baseQuery.deleteCharAt(baseQuery.length()-1);
        baseQuery.append("\")");
        baseQuery.append(".vertices().collect{ids.add(it.getElement().id())};vertexList = ids.toArray();if (vertexList.size()==0){return};");

        return baseQuery.toString();
    }


    private String dealWithWords(String words) {
        if (words == null) return "";
        if ("".equals(words.trim()) || ",".equals(words.trim())) return "";
        words=words.replaceAll(","," ");
        StringBuffer query = new StringBuffer();
        for (WordsCover field : WordsCover.values()) {
            query.append("v.\\\"");
            query.append(field);
            query.append("\\\":(");
            query.append(words.replaceAll(",", ""));
            query.append(") ");
        }

        return query.toString();
    }


    public enum WordsCover {
        title, description, keywords, tags, edu_description, cr_description
    }
}
