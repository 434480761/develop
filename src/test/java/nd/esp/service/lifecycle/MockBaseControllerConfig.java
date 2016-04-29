package nd.esp.service.lifecycle;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class MockBaseControllerConfig  extends BaseControllerConfig{
    protected boolean close_init=true;
    
    @Before
    public void before() {
        if(!close_init){
            super.before();
        }
      MockitoAnnotations.initMocks(this);  
    }
    
    @After
    public void after() {
        if(!close_init){
            super.after();
        }
    }
}
