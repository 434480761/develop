package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanIndexOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TitanIndexServiceImpl implements TitanIndexService {
	

	@Autowired
	private TitanIndexOperation titanIndexOperation;
	
	@Override
	public boolean createScehma() {
		return titanIndexOperation.createSchema();
	}

}
