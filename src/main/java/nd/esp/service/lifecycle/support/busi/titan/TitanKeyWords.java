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
    , has_chapter,has_knowledge,tech_info,has_tech_info,has_knowledge_relation,
    
    //资源冗余字段
    search_code,search_path,search_coverage,search_code_string,search_path_string,search_coverage_string,

    //树形结构
    tree_has_chapter,tree_has_knowledge,tree_order ;
}
