package com.my.client;

import java.io.Serializable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.my.server.Server;
import com.remote.Remote;
import com.remote.RemoteImpl;

public class ClientTest {

	private Server rmiServer;
    private Client rmiClient;

    public interface Interface{

        String testMethod1();

        void testMethod2( String arg1, int arg2 );
        public String testMethod3(String arg1, int arg2);
    }
    
    public class InterfaceImpl implements Interface{
		@Override
		public String testMethod1() {
			return "Hello";
		}

		@Override
		public void testMethod2(String arg1, int arg2) {
			System.out.println(arg1 +" : "+ arg2);
		}
		
		@Override
		public String testMethod3(String arg1, int arg2) {
			return arg1 + arg2;
		}
    	
    }

    @Before
    public void setUp(){

    }

    @After
    public void cleanUp(){
//        rmiClient.disconnect();
//        rmiServer.stop();
    }

    // XXX 我不知道怎么回事, 测试在junit这里会出现错误, 而在main方法里面则没事, 也可能junit没有出现错误
    // 而是没有在控制台出现想要的信息, 这个可能跟junit和mian函数在显示器显示数据规则有关
    @Test
    public void callMethodWithNoArgumentAndReturnValue(){
		int port = 8989;
		rmiServer = new Server(port);
		rmiClient = new Client( "localhost", port );
        final String RESULT_TEST_METHOD1 = "Hello";
        InterfaceImpl impl = new InterfaceImpl();
//        Interface impl = mock( Interface.class );
//        String s = impl.testMethod1();
//        System.out.println("oooo"+s);
//        when( impl.testMethod1() ).thenReturn( RESULT_TEST_METHOD1 );
//        when( impl.testMethod1() ).thenReturn( "World" );
        rmiServer.addImplementation( Interface.class, impl );
        Interface clientProxy = rmiClient.getImplementation( Interface.class );
        String r = clientProxy.testMethod3("aa", 888);
//        String returnValue = clientProxy.testMethod1();
//        System.out.println(returnValue);
        System.out.println(r);
//        assertEquals( RESULT_TEST_METHOD1, returnValue );
    }
    
    @Test
    public void testServer(){
    	int port = 8989;
        rmiServer = new Server( port );
        Remote impl = new RemoteImpl();
        rmiServer.addImplementation( Remote.class, impl);
    }
    
    @Test
    public void testClient(){
      int port = 8989;
      rmiClient = new Client( "localhost", port );
      Remote clientProxy = rmiClient.getImplementation( Remote.class );
      String r = clientProxy.method("aa", "11");
      System.out.println(r);
    }
    
    @Test
    public void testAll(){
    	int port = 8989;
    	rmiServer = new Server( port );
    	Remote impl = new RemoteImpl();
    	rmiServer.addImplementation( Remote.class, impl);
    	
    	rmiClient = new Client( "localhost", port );
    	Remote clientProxy = rmiClient.getImplementation( Remote.class );
    	String r = clientProxy.method("aa", "11");
    	System.out.println(r);
    }

    @Test
    public void callVoidMethodWithArguments(){
        final String ARG1 = "ARG1";
        final int ARG2 = 123456;
//        Interface impl = mock( Interface.class );
        Interface impl = new InterfaceImpl();
        rmiServer.addImplementation( Interface.class, impl);
//        rmiServer.addImplementation( Interface.class, impl);
//        Interface clientProxy = rmiClient.getImplementation( Interface.class );
        Interface clientProxy = rmiClient.getImplementation( Interface.class );
        System.out.println("haha");
        clientProxy.testMethod2( ARG1, ARG2 );
    }

    @Test
    public void testThrow(){
    	MainTest mt = new MainTest();
    	mt.throwException();
    }
}
