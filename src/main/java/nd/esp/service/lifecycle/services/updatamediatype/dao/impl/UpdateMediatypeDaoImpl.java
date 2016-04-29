package nd.esp.service.lifecycle.services.updatamediatype.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.services.updatamediatype.dao.UpdateMediatypeDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.TechInfo;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategoryRepository;
import nd.esp.service.lifecycle.repository.sdk.TechInfoRepository;

@Repository
public class UpdateMediatypeDaoImpl implements UpdateMediatypeDao {
    @Autowired
    private ResourceCategoryRepository resourceCategoryRepository;

    @Autowired
    private TechInfoRepository techInfoRepository;

    // public Map<String , > getEntityByPage(){
    //
    // }

    /**
     * 查询resource_categories表中的所有数据，取出包含Mediatype的资源
     * */
    public Map<String, ResourceCategory> getAllCategory() {
        String sql = "SELECT resource , category_name ,category_code ,short_name FROM resource_categories where category_name='mediatype' ";
        final Map<String, ResourceCategory> map = new HashMap<String, ResourceCategory>();
        resourceCategoryRepository.getJdbcTemple().query(sql, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                ResourceCategory rc = new ResourceCategory();

                rc.setResource(rs.getString("resource"));
                rc.setCategoryName(rs.getString("category_name"));
                rc.setCategoryCode(rs.getString("category_code"));
                rc.setShortName(rs.getString("short_name"));
                map.put(rc.getResource(), rc);

                return null;
            }
        });
        return map;
    }

    /**
     * tech_info表映射map,使用Resource作为key
     * */
    public Map<String, List<TechInfo>> getAllTechInfo() {

        String sql = "SELECT identifier , format , resource FROM tech_infos";

        final Map<String, List<TechInfo>> map = new HashMap<String, List<TechInfo>>();

        techInfoRepository.getJdbcTemple().query(sql, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {

                List<TechInfo> list = null;
                TechInfo techInfo = new TechInfo();

                techInfo.setIdentifier(rs.getString("identifier"));
                techInfo.setFormat(rs.getString("format"));
                techInfo.setResource(rs.getString("resource"));

                list = map.get(techInfo.getResource());

                if (list == null) {
                    list = new LinkedList<TechInfo>();
                    list.add(techInfo);
                    map.put(techInfo.getResource(), list);
                }
                else {
                    list.add(techInfo);
                }

                return null;
            }
        });

//        // 验证
//        int count = 0;
//        for (String id : map.keySet()) {
//            int size = map.get(id).size();
//            if (size > 1) {
//                System.out.println(id + "--" + size);
//            }
//            count = count + size;
//        }
//
//        System.out.println(count);

        return map;

    }

    public Map<String, List<ResourceCategory>> getAllCategories() {
        String sql = "SELECT identifier , category_code , category_name , resource , short_name FROM resource_categories WHERE category_name ='mediatype'";
        final Map<String, List<ResourceCategory>> map = new HashMap<String, List<ResourceCategory>>();

        techInfoRepository.getJdbcTemple().query(sql, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {

                List<ResourceCategory> list = null;
                ResourceCategory category = new ResourceCategory();

                category.setCategoryCode(rs.getString("category_code"));
                category.setCategoryName(rs.getString("category_name"));
                category.setResource(rs.getString("resource"));
                category.setShortName(rs.getString("short_name"));
                category.setIdentifier("identifier");

                list = map.get(category.getResource());

                if (list == null) {
                    list = new LinkedList<ResourceCategory>();
                    list.add(category);
                    map.put(category.getResource(), list);
                }
                else {
                    list.add(category);
                }
                return null;
            }
        });
        
        // 验证
        int count = 0;
        for (String id : map.keySet()) {
            int size = map.get(id).size();
            count = count + size;
        }
        return map;
    }

}
