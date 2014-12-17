package com.my.server;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.my.server.internal.ImplementationContainer;
import com.my.server.internal.NewSocketCallback;
import com.my.server.internal.ServerSocketManager;
import com.my.server.internal.SingleConnectionHandler;
import com.my.shared.InvokeMethodRequest;
import com.my.shared.InvokeMethodResponse;
import com.my.shared.InvokeMethodResponse.Type;
import com.remote.Minus;
import com.remote.MinusImpl;
import com.remote.Remote;
import com.remote.RemoteImpl;

public class Server {
	private final int port;
	private final ImplementationContainer implContainer = new ImplementationContainer();
	private final List<SingleConnectionHandler> connectionHandlers = new ArrayList<SingleConnectionHandler>();
	private final ServerSocketManager serverSocketManager;
	private final NewSocketCallback newSocketCallback;
	
	public Server(int port){
		this.port = port;
		this.newSocketCallback = new NewSocketCallback() {
			@Override
			public void newSocketAccepted(Socket newSocket) {
				Server.this.newSocketAccept(newSocket);
			}
		};
		serverSocketManager = new ServerSocketManager(this.port, newSocketCallback);
		serverSocketManager.start();
	}
	
	private void newSocketAccept(Socket newSocket){
		System.out.println("newSocketAccept load");
		SingleConnectionHandler connectionHandler = new SingleConnectionHandler(newSocket, implContainer);
		connectionHandler.start();
		connectionHandlers.add(connectionHandler);
	}
	
	public void stop(){
		serverSocketManager.stop();
	}
	
	public <T> void addImplementation(Class<T> interfaceClazz, T implementation){
		if( interfaceClazz == null ) throw new NullPointerException( "interfaceClazz is null" );
        if( implementation == null ) throw new NullPointerException( "implementation is null" );
        if( !interfaceClazz.isInterface() ) throw new IllegalArgumentException( "no interface" );
        if( implContainer.containsImplementation( interfaceClazz ) ) throw new IllegalArgumentException( "duplicate implementation" );

        implContainer.addImplementation( interfaceClazz, implementation );
	}
	
	public int getPort(){
		return this.port;
	}
	
	public static void main(String[] args) throws Exception{
    	int port = 5010;
        Server rmiServer = new Server( port );
        Remote impl = new RemoteImpl();
        rmiServer.addImplementation( Remote.class, impl);
        Minus mi = new MinusImpl();
        rmiServer.addImplementation(Minus.class, mi);
	}
	
	public static void mains(String[] args) throws Exception {
		ServerSocket server = new ServerSocket(5010);
		boolean isOpen = true;
		while (isOpen) {
			Socket socket = server.accept();
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						handle();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				private void handle() throws Exception {
					System.out.println("==================");
					InputStream input = socket.getInputStream();
//					InputStreamReader reader = new InputStreamReader(input);
					OutputStream output = socket.getOutputStream();
//					OutputStreamWriter writer = new OutputStreamWriter(output);

					boolean isClosed = false;
					while (!isClosed) {
						ObjectInputStream ois = new ObjectInputStream(input);
						ObjectOutputStream oos = new ObjectOutputStream(output);
						// 注意socket的流是在while循环外的, 而序列化的流是在while内部
						// 如果socket存在, 则while不断循环以便从soket流中读取数据再用序列化流进行包装
						// 这里可以看看simplerpc里面StreamUtil与client和server端的调用关系
						InvokeMethodRequest request = (InvokeMethodRequest)ois.readObject();
						request.getMethodName();
						Class<?> clazz = Class.forName(request.getInterfaceName()+"Impl");
						Object obj = clazz.newInstance();
						Class<?>[] paramTypes = request.getParameterTypesAsArray();
						String methodName = request.getMethodName();
						Method method = clazz.getMethod(methodName, paramTypes);
						Object res = method.invoke(obj, request.getArgumentsAsArray());
						InvokeMethodResponse response = new InvokeMethodResponse(res, null, Type.OK);
						oos.writeObject(response);
						oos.flush();
//						oos.close();
//						ois.close();
					}
				}
			});
			t.start();
		}
		server.close();
	}
}
