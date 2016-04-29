package nd.esp.service.lifecycle.controllers.staticdatas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.support.StaticDatas;
import nd.esp.service.lifecycle.vos.staticdatas.StaticDatasViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staticdatas")
public class StaticDatasController {
	private final static Logger LOG= LoggerFactory.getLogger(StaticDatasController.class);
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	/**
	 * 返回静态变量开关 -- 用于页面显示
	 * @return
	 */
	@RequestMapping(value="/show",method=RequestMethod.GET)
	public List<StaticDatasViewModel> showStaticDatas(){
		List<StaticDatasViewModel> list = new ArrayList<StaticDatasViewModel>();
		
		List<String> names = getStaticDatasName();
		StaticDatas staticDatas = new StaticDatas();
		for(String name : names){
			try {
				StaticDatasViewModel sdvm = new StaticDatasViewModel();
				sdvm.setName(name);
				sdvm.setValue(StaticDatas.class.getField(name).getBoolean(staticDatas));
				list.add(sdvm);
			} catch (IllegalArgumentException e) {
				LOG.error(e.getMessage());
			} catch (IllegalAccessException e) {
				LOG.error(e.getMessage());
			} catch (NoSuchFieldException e) {
				LOG.error(e.getMessage());
			} catch (SecurityException e) {
				LOG.error(e.getMessage());
			}
		}
		
		return list;
	}
	
	/**
	 * 获取静态变量名
	 * @return
	 */
	private List<String> getStaticDatasName(){
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
}
