package com.my.server.internal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import com.my.exception.SimpleRpcException;
import com.my.server.Server;

public class ServerSocketManager {
	private static int DEFAULT_NUM_HANDLERS = 20;
	private static int DEFAULT_CONCURRENT_REQUESTS = 1;
	
	private final static Logger logger = Logger.getLogger(Server.class.getName());
	private final ServerSocket serverSocket;
	
	private final int port;
	private final NewSocketCallback newSocketCallback;
	// 为什么这里不是final,而上面是呢?? 因为final的需要一开始就初始化, 加上final也是为了保证这个, 跟js的setTimeout(func, 0)似的
	private Thread acceptingThread;

	public ServerSocketManager(ServerSocket serverSocket, int port, NewSocketCallback newSocketCallback) {
		this.port = port;
		this.newSocketCallback = newSocketCallback;
	}

	public void start() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SimpleRpcException(e);
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println("SocketManager thread running!");
				listenForNewConnections(serverSocket);
			}
		};
		this.acceptingThread = new Thread(runnable);
		this.acceptingThread.start();
		
		// 添加了这个之后, 会一直阻塞走不到addImplementation()方法里面
//		try {
//			acceptingThread.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	public void listenForNewConnections(ServerSocket serverSocket) {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("get client socket!!");
				newSocketCallback.newSocketAccepted(socket);
			} catch (IOException e) {
				e.printStackTrace();
				if (checkForSocketClosed(e)) {
					return;
				}
			}
		}
	}

	private boolean checkForSocketClosed(IOException e) {
		if (!(e instanceof SocketException))
			return false;
		if (!e.getMessage().contains("Socket closed"))
			return false;
		return true;
	}

	public void stop() {
		if( acceptingThread != null ) acceptingThread.interrupt();
		if( serverSocket == null ) return;
        try{
            serverSocket.close();
        }
        catch( IOException e ){
            e.printStackTrace();
        }
	}

}
