package nd.esp.service.lifecycle.support.busi.titan;

/**
 * 用于放置保留字段与标签
 * 
 * @author linsm
 */
public enum TitanKeyWords {
    g, V, hasLabel, has, or, and, as, select, inE, outE, outV, inV, within, without,
    order, by, incr, decr, limit, range,
    lt, lte, gt, gte, textRegex,textContains,
    RESULT,TOTALCOUNT,
    //暂时放在这里
    has_relation, has_category_code, category_code, has_coverage, coverage, categories_path, has_categories_path
    ,tech_info,has_tech_info,has_knowledge_relation,// has_chapter,has_knowledge,
    
    //资源冗余字段
    search_code,search_path,search_coverage,search_code_string,search_path_string,search_coverage_string,

    primary_category,
    // 边上的几个属性
    relation_type,rr_label,order_num,sort_num,

    //树形结构
    tree_has_chapter("tree_has_chapters"),tree_has_knowledge("tree_has_knowledges"),tree_order("tree_order_num"),
    //统计
    statistical,has_resource_statistical,
    // order by的几个条件字段和值
    sta_key_title,sta_data_from,ti_title,href,ti_size
    ;

    private String name;

    private TitanKeyWords(String name){
        this.name = name;
    }
    private TitanKeyWords(){

    }

    @Override
    public String toString() {
        if(name == null){
            return super.toString();
        }else{
            return name;
        }
    }

    public static void main(String[] args){
        for(TitanKeyWords keyWords:TitanKeyWords.values()){
            System.out.println(keyWords.toString());
        }
    }
}
