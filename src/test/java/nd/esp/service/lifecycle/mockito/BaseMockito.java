package nd.esp.service.lifecycle.mockito;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @title 完成@see {@link Mock} 标签的初始化工作
 * <p>第一种 MockitoAnnotations.initMocks(this)</p>
 * <p>第二种 @RunWith(MockitoJUnitRunner.class)</p>
 * @desc
 * @atuh lwx
 * @createtime on 2015年7月21日 下午4:58:27
 */
//@RunWith(MockitoJUnitRunner.class)  
public class BaseMockito {
    
    @Mock
    protected List mockList;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    
    @Test
    public void test(){
        mockList.add(1);
        Mockito.verify(mockList).add(1);
        
    }
}
