package nd.esp.service.lifecycle.services.staticdatas.impls;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.models.CategoryPatternModel;
import nd.esp.service.lifecycle.models.ivc.v06.IvcConfigModel;
import nd.esp.service.lifecycle.services.staticdatas.StaticDataService;
import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class StaticDataServiceImpl implements StaticDataService {
	private final static Logger LOG = LoggerFactory.getLogger(StaticDataServiceImpl.class);

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	
	/**
     *  获取静态变量的现在状态
     * <p>Create Time: 2016年3月2日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
	@Override
    public List<Map<String, Integer>> queryNowStatus(){
        final List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
        
        String sql = "SELECT name,status FROM static_datas";
        jdbcTemplate.query(sql, new RowMapper<String>(){

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, Integer> map = new HashMap<String, Integer>();
                map.put(rs.getString("name"), rs.getInt("status"));
                list.add(map);
                return null;
            }
            
        });
        
        return list;
    }
	
	/**
	 * 获取静态变量名
	 */
	public List<String> getStaticDatasName(){
		final List<String> list = new ArrayList<String>();
        
        String sql = "SELECT name FROM static_datas";
        jdbcTemplate.query(sql, new RowMapper<String>(){

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                list.add(rs.getString("name"));
                return null;
            }
            
        });
        
        return list;
	}
    
    /**
     * 获取上次的更新时间
     * <p>Create Time: 2016年3月2日   </p>
     * <p>Create author: xiezy   </p>
     * @return
     */
	@Override
    public Long queryLastUpdateTime(int taskId){
        final Map<String, Long> map = new HashMap<String, Long>();
        
        String sql = "SELECT last_update FROM static_datas_update WHERE taskId=" + taskId;
        jdbcTemplate.query(sql, new RowMapper<Long>(){

            @Override
            public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                map.put("lastUpdate", rs.getLong("last_update"));
                return null;
            }
            
        });
        
        return map.get("lastUpdate");
    }
    
    /**
     * 利用反射给静态变量设值
     * <p>Create Time: 2016年3月2日   </p>
     * <p>Create author: xiezy   </p>
     * @param name
     * @param value
     */
	@Override
    public void setValues(String name,Integer value){
        try {
            Field field = StaticDatas.class.getField(name);
            field.setBoolean(null, value==1 ? true : false);
        } catch (NoSuchFieldException e) {
            LOG.error(e.getMessage());
        } catch (SecurityException e) {
            LOG.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage());
        } catch (IllegalAccessException e) {
            LOG.error(e.getMessage());
        }
    }
	
	/**
	 * 通过反射获取静态变量值
	 */
	public boolean getValues(String name){
		try {
			Field field = StaticDatas.class.getField(name);
			return field.getBoolean(null);
		} catch (NoSuchFieldException e) {
			LOG.error(e.getMessage());
		} catch (SecurityException e) {
			LOG.error(e.getMessage());
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage());
		} catch (IllegalAccessException e) {
			LOG.error(e.getMessage());
		}
		return false;
	}
	
	/**
     * 修改静态变量的当前状态	
     * <p>Create Time: 2016年3月2日   </p>
     * <p>Create author: Administrator   </p>
     * @param jdbcTemplate
     * @param name
     * @param values
     */
	@Override
	public void updateNowStatus(String name, int value) {
        String sql = "UPDATE static_datas SET status=" + value + " WHERE name='" + name + "'";
        jdbcTemplate.execute(sql);
    }
    
	/**
     * 修改上次更新时间	
     * <p>Create Time: 2016年3月2日   </p>
     * <p>Create author: xiezy   </p>
     */
	@Override
	public void updateLastTime(int taskId) {
        long now = System.currentTimeMillis();
        String sql = "UPDATE static_datas_update SET last_update=" + now + " WHERE taskId=" + taskId;
        jdbcTemplate.execute(sql);
    }
    
	/**
	 * 立即更新IVC_CONFIG_MAP
	 */
	@Override
	public void updateIvcMapNow() {
		flashIvcConfigMap(StaticDatas.IVC_CONFIG_MAP);
	}
	
	/**
	 * 立即更新IVC_USER_MAP
	 */
	@Override
	public void updateIvcUserMapNow() {
		flashIvcUserMap(StaticDatas.IVC_USER_MAP);
	}
	
	/**
     * 获取所有的IvcConfig数据
     */
	@Override
    public void flashIvcConfigMap(final Map<String, IvcConfigModel> configMap){ 	
    	String sql = "SELECT bsyskey,bsysivcconfig FROM third_party_bsys";
    	final Set<String> keySet = new HashSet<String>();
    	jdbcTemplate.query(sql, new RowMapper<IvcConfigModel>(){

			@Override
			public IvcConfigModel mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				configMap.put(rs.getString("bsyskey"), 
						CommonHelper.convertJson2IvcConfig(rs.getString("bsysivcconfig")));
				keySet.add(rs.getString("bsyskey"));
				
				return null;
			}
    		
    	});
    	
    	if(keySet.size()!=configMap.size()) {
	    	for(String key:configMap.keySet()) {
	    		if(!keySet.contains(key)) {
	    			configMap.remove(key);
	    		}
	    	}
    	}
    }
	
	/**
	 * 获取所有bsyskey与userId的对应
	 */
	@Override
	public void flashIvcUserMap(final Map<String, String> ivcUserMap) {
		String sql = "SELECT bsyskey,user_id FROM third_party_bsys";
    	final Set<String> keySet = new HashSet<String>();
    	jdbcTemplate.query(sql, new RowMapper<IvcConfigModel>(){

			@Override
			public IvcConfigModel mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				if(StringUtils.hasText(rs.getString("user_id"))){
					ivcUserMap.put(rs.getString("user_id"), rs.getString("bsyskey"));
					keySet.add(rs.getString("user_id"));
				}
				
				return null;
			}
    		
    	});
    	
    	if(keySet.size() != ivcUserMap.size()) {
	    	for(String key:ivcUserMap.keySet()) {
	    		if(!keySet.contains(key)) {
	    			ivcUserMap.remove(key);
	    		}
	    	}
    	}
		
	}
	
	/**
	 * 获取所有的维度模式
	 */
	@Override
	public Map<String, CategoryPatternModel> getCategoryPatternMap() {
		final Map<String, CategoryPatternModel> map = new HashMap<String, CategoryPatternModel>();
		
		String sql = "SELECT identifier,description,title,pattern_name,pattern_path,purpose,scope,segment "
				+ "FROM category_patterns";
		jdbcTemplate.query(sql, new RowMapper<CategoryPatternModel>(){

			@Override
			public CategoryPatternModel mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				CategoryPatternModel cpm = new CategoryPatternModel();
				cpm.setIdentifier(rs.getString("identifier"));
				cpm.setDescription(rs.getString("description"));
				cpm.setTitle(rs.getString("title"));
				cpm.setPatternName(rs.getString("pattern_name"));
				cpm.setPatternPath(rs.getString("pattern_path"));
				cpm.setPurpose(rs.getString("purpose"));
				cpm.setScope(rs.getString("scope"));
				cpm.setSegment(rs.getString("segment"));
				map.put(rs.getString("pattern_name"), cpm);
				return null;
			}
			
		});
		
		return map;
	}

	@Override
	public void updateCPMapNow() {
		StaticDatas.CATEGORY_PATTERN_MAP = getCategoryPatternMap();
	}
}
