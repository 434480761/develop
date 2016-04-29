package nd.esp.service.lifecycle.mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;

/**
 * @title Mockito测试
 * @desc
 * @atuh lwx
 * @createtime on 2015年7月17日 下午5:23:02
 */
public class TestMockito extends BaseMockito {
	private static final Logger LOG = LoggerFactory.getLogger(TestMockito.class);

    /**	
     * @desc:测试mockito的行为  
     * @createtime: 2015年7月20日 
     * @author: liuwx 
     * @throws EspStoreException
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test
    public void testBehaviour() {
        LOG.info("*****测试mockito的行为*****");
        List mockList = mock(List.class);
        mockList.add("abc");
        mockList.clear();
        verify(mockList).add("abc");
        verify(mockList).clear();
    }

    /**	
     * @desc:测试mockito的stubbing
     * @createtime: 2015年7月20日 
     * @author: liuwx 
     * @throws EspStoreException
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test(expected = RuntimeException.class)
    public void testStubbing() {
        LOG.info("*****测试mockito stub*****");
        List mockList = mock(List.class);
        //mock get(0) excepted abc
        when(mockList.get(0)).thenReturn("abc");
        when(mockList.get(1)).thenThrow(new RuntimeException("run"));
        assertEquals("模拟get(0)返回abc", "abc", mockList.get(0));
        assertNull("未定义get(66)", mockList.get(66));
        verify(mockList).get(0);
        mockList.get(1);// throw runtimeexception
        
    }

    /**	
     * @desc:测试mockito的argument机制
     * @createtime: 2015年7月20日 
     * @author: liuwx 
     * @throws EspStoreException
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test
    public void testArgument() {
        LOG.info("*****测试mockito参数*****");
        List mockList = mock(List.class);
        when(mockList.get(anyInt())).thenReturn("everyInt");
        
        assertEquals("模拟get(0)返回everyInt", "everyInt", mockList.get(0));
        // System.out.println(mockList.get(1));//throw runtimeexception
        assertEquals("模拟get(66)返回everyInt", "everyInt", mockList.get(66));
        
        when(mockList.contains(argThat(new ListArgumentMatcher()))).thenReturn(true);
        //由于上面一句，导致下面contains返回的都是true
        assertEquals(true,mockList.contains(anyString()));
        verify(mockList).get(66);
        ArgumentDemo demo = mock(ArgumentDemo.class);
        demo.argumentTest("test", 6, true);
        //这种也支持
        //verify(demo).argumentTest(anyString(), anyInt(), eq(true));
        verify(demo).argumentTest(Matchers.any(String.class), anyInt(), eq(true));
        // 报错，真实值必须是使用eq包装
        // verify(demo).argumentTest(anyString(),anyInt(),true);
    }

    /**	
     * @desc:校验触发次数/至少x/从来没有  
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test
    public void testNumber() {

        List mockList = mock(List.class);
        mockList.add("once");

        mockList.add("twice");
        mockList.add("twice");

        mockList.add("three times");
        mockList.add("three times");
        mockList.add("three times");

        verify(mockList).add("once");// 默认一次 verify(mock, times(1))
        verify(mockList, times(2)).add("twice");
        // 从来没有执行过
        verify(mockList, never()).add("never happened");

        verify(mockList, atLeastOnce()).add("three times");
        verify(mockList, atLeast(3)).add("three times");

    }

    /**    
     * @desc:校验异常
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test(expected = RuntimeException.class)
    public void testException() {
        List mockList = mock(List.class);
        doThrow(new RuntimeException()).when(mockList).clear();
        when(mockList.get(1)).thenThrow(new RuntimeException("run"));
        mockList.clear();

    }

    /**    
     * @desc:校验触发顺序
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test()
    public void testOrder() {
        // 单个校验
        List singleMock = mock(List.class);
        // using a single mock
        singleMock.add("was added first");
        singleMock.add("was added second");

        // create an inOrder verifier for a single mock
        InOrder inOrder = inOrder(singleMock);

        // following will make sure that add is first called with "was added first, then with "was added second"
        inOrder.verify(singleMock).add("was added first");
        inOrder.verify(singleMock).add("was added second");

        List list = mock(List.class);
        List list2 = mock(List.class);
        list.add(1);
        list2.add("hello");
        list.add(2);
        list2.add("world");
        // 将需要排序的mock对象放入InOrder
        inOrder = inOrder(list, list2);
        // 下面的代码不能颠倒顺序，验证执行顺序
        inOrder.verify(list).add(1);
        inOrder.verify(list2).add("hello");
        inOrder.verify(list).add(2);
        inOrder.verify(list2).add("world");

    }

    /**    
     * @desc:验证无互动行为(没有执行到)
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test
    public void verify_interaction() {
        List list = mock(List.class);
        List list2 = mock(List.class);
        List list3 = mock(List.class);
        // list3.isEmpty();//开启会报错
        list.add(1);
        verify(list).add(1);
        verify(list, never()).add(2);
        // 验证零互动行为
        verifyZeroInteractions(list2, list3);
        // 官方不推荐在极少维护以外的单元测试中使用verifyZeroInteractions,更多的应该使用never()
    }

    /**    
     * @desc:找出冗余的互动(未被验证的)
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test(expected = NoInteractionsWanted.class)
    public void find_redundant_interaction() {
        List list = mock(List.class);
        list.add(1);
        list.add(2);
        verify(list, times(2)).add(anyInt());
        // 检查是否有未被验证的互动行为，因为add(1)和add(2)都会被上面的anyInt()验证到，所以下面的代码会通过
        verifyNoMoreInteractions(list);

        List list2 = mock(List.class);
        list2.add(1);
        list2.add(2);
        verify(list2).add(1);
        // 检查是否有未被验证的互动行为，因为add(2)没有被验证，所以下面的代码会失败抛出异常
        verifyNoMoreInteractions(list2);
        //加上 verify(list2).add(2); verifyNoMoreInteractions(list2);就不会报错了
        //
        
        
        
    }

    /**    
     * @desc:全局设置
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test()
    public void testShorthand() {
        when(mockList.get(1)).thenReturn(2);
        assertEquals("期待返回2", 2,  mockList.get(1));
        verify(mockList).get(1);

    }

    /**    
     * @desc:全局设置
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test(expected = RuntimeException.class)
    public void testConsecutive() {
        when(mockList.get(1)).thenReturn(1);
        when(mockList.get(1)).thenReturn(2);
        when(mockList.get(1)).thenReturn(3);
        System.out.println("返回3--" + mockList.get(1));
        when(mockList.get(1)).thenReturn(5, 6);
        System.out.println("返回5--" + mockList.get(1));
        System.out.println("返回6--" + mockList.get(1));
        when(mockList.get(1)).thenReturn(7).thenThrow(new RuntimeException());
        System.out.println("返回7--" + mockList.get(1));
        mockList.get(1);

    }

    /**    
     * @desc:自定义回调
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test()
    public void testCallback() {

        when(mockList.get(anyInt())).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // TODO Auto-generated method stub
                Object arg[] = invocation.getArguments();
                return "arg--" + arg[0];
            }
        });
        Assert.assertEquals("arg--66", mockList.get(66));
        Assert.assertEquals("arg--77", mockList.get(77));

    }

    /**    
     * @desc:另外一种mock模拟方式
     * 标准使用when()开头
     * <p> doReturn(Object)</p>
     * <p> doThrow(Throwable)</p>
     * <p> doThrow(Class)</p>
     * <p> doAnswer(Answer)</p>
     * <p> doNothing()</p>
     * <p> doCallRealMethod()</p>
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test()
    public void testDoMethod() {
        when(mockList.get(1)).thenReturn(1);
        Mockito.doReturn(2).when(mockList).get(2);
        Assert.assertSame(1, mockList.get(1));
        Assert.assertSame(2, mockList.get(2));

        ArgumentDemo demo = mock(ArgumentDemo.class);

        // donothing只针对void类型的方法
        Mockito.doNothing().when(demo).nothingTest("");
        when(demo.argumentTest("", 1, true)).thenReturn(2);
        System.out.println(demo.argumentTest(anyString(), anyInt(), eq(true)));
    }

    /**	
     * @desc:测试spy的用法，spy是可以调用真实对象的api，需要mock的时候必须用do打头而不是when  
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test()
    public void testSpy() {
        List list = new LinkedList();
        List spy = spy(list);

        // optionally, you can stub out some methods:
        when(spy.size()).thenReturn(100);

        // using the spy calls *real* methods
        spy.add("one");
        spy.add("two");

        // prints "one" - the first element of a list
        assertEquals("返回one","one", spy.get(0));

        // size() method was stubbed - 100 is printed
        assertEquals("返回one",100, spy.size());

        // optionally, you can verify
        verify(spy).add("one");
        verify(spy).add("two");

        List spy2 = spy(list);
        // 抛出IndexOutOfBoundsException 异常 spy2相当于copy from list(并且还是一个空的list)
        // when(spy2.get(0)).thenReturn("foo");
        // 正确的做法
        doReturn("doReturn").when(spy2).get(0);
        Assert.assertEquals("测试spy doreturn方法", "doReturn", spy2.get(0));
    }

    /**    
     * @desc:mock stub expected某个默认值
     * @createtime: 2015年7月21日 
     * @author: liuwx 
     */
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    @Test()
    public void testInvocation() {
        // 第二个参数，类似spring getbean中的beanfactory，可以改变默认的值
        List mock = mock(List.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return 666;
            }

        });
        Assert.assertEquals(666, mock.size());
        Assert.assertEquals(666, mock.get(2));

    }

}

class ListArgumentMatcher extends ArgumentMatcher<List> {

    @Override
    public boolean matches(Object argument) {
        // TODO Auto-generated method stub
        // return ((List) argument).size() == 1;
        return true;
    }

}

class ArgumentDemo {

    public int argumentTest(String str, int a, boolean b) {
        System.out.println("argumentTest");
        System.out.println(str);
        System.out.println(a);
        System.out.println(b);
        return 1;

    }

    public void nothingTest(String str) {
        System.out.println("nothingTest:" + str);

    }
}
