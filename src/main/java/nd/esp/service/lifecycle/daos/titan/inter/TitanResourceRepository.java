package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.Education;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;


public interface TitanResourceRepository<M extends Education> extends TitanEspRepository<M> {
	
	boolean delete(String primaryCategory, String identifier);
	
	long count(String primaryCategory);
	
	Vertex get(String primaryCategory,String identifier);
	
	//FIXME 
	ResultSet search(String script,Map<String, Object> scriptParamMap);

}
