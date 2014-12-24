package com.my.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {
	private static int DEFAULT_NUM_HANDLERS = 20;
	private static int DEFAULT_CONCURRENT_REQUESTS = 1;
	
	private final static Logger logger = Logger.getLogger(Server.class.getName());
	private final ServerSocket serverSocket;
	private final ExecutorService incomingHandlerPool;
	private final ExecutorService outgoingHandlerPool = 
			Executors.newCachedThreadPool();
	private final ExecutorService requestHandlerPool;
	private final ImplementationContainer implContainer = new ImplementationContainer();
	private final Set<ConnectionHandler> activeConnections = 
			Collections.synchronizedSet(new HashSet<ConnectionHandler>());
	
	public static Server create(int port){
		return create(port, DEFAULT_NUM_HANDLERS, DEFAULT_CONCURRENT_REQUESTS);
	}
	
	public static Server create(int port, int maxNumHandlers, int maxConcurrentRequests){
		try {
			InetSocketAddress address = new InetSocketAddress(port);
			ServerSocket serverSocket;
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(address);
			/**
			 * 注意做incoming/request/outgoing处理的三个线程池都是在Server初始化的时候实例化的
			 * 这保证了三个线程池只在Server初始化的时候生成一次
			 */
			ExecutorService incomingHandlerPool =
                    Executors.newFixedThreadPool(maxNumHandlers);
            ExecutorService requestHandlerPool =
                    Executors.newFixedThreadPool(maxConcurrentRequests);
            return new Server(serverSocket, incomingHandlerPool, requestHandlerPool);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not create server.", e);
			return null;
		}
	}
	
	public Server(ServerSocket serverSocket, 
            ExecutorService incomingHandlerPool,
            ExecutorService requestHandlerPool) {
        this.serverSocket = serverSocket;
        this.incomingHandlerPool = incomingHandlerPool;
        this.requestHandlerPool = requestHandlerPool;
    }
	
	public int getPort() {
		return serverSocket.getLocalPort();
	}
	
	public <T> void addImplementation(Class<T> interfaceClazz, T implementation){
        implContainer.addImplementation( interfaceClazz, implementation );
	}
	
	private synchronized void handleConnection(Socket connection){
		if(serverSocket.isClosed()){
			return;
		}
			
		final ConnectionHandler handler = ConnectionHandler.create(connection, requestHandlerPool, outgoingHandlerPool, implContainer);
		activeConnections.add(handler);
		final Runnable realIncomingHandler = handler.createIncomingHandler();
		
		class HelperHandler implements Runnable{
			@Override
			public void run() {
				activeConnections.add(handler);
				try{
					realIncomingHandler.run();
				}finally{
					activeConnections.remove(handler);
				}
			}
		}
		
		incomingHandlerPool.execute(new HelperHandler());
	}
	
	@Override
	public synchronized void interrupt() {
		super.interrupt();
		for(ConnectionHandler handler : activeConnections){
			handler.closeConnection();
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error closing socket.", e);
		}
	};
	
	@Override
	public void run(){
		logger.info("Running server on port "+serverSocket.getLocalPort());
		while(!serverSocket.isClosed()){
			try {
				Socket connection = this.serverSocket.accept();
				handleConnection(connection);
			} catch (IOException e) {
				logger.log(Level.WARNING, "Could not establish connection.", e);
			}
		}
		logger.info("Server exits.");
	}
}
