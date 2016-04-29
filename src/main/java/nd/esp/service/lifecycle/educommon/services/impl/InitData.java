package nd.esp.service.lifecycle.educommon.services.impl;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class InitData {
    @Autowired
    JdbcTemplate jt;
    
    @PostConstruct
    private void initSynData(){
    	String sql = "update synchronized_table set value = 0";
    	jt.execute(sql);
    } 
}
