package com.my.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.my.shared.Request;
import com.my.shared.Response;

public class ConnectionHandler {
	private final Socket connection;
	private final RequestDispatcher dispatcher;
	private final ExecutorService outgoingHandlerPool;
	
	private class IncomingHandler implements Runnable{
		@Override
		public void run(){
			try{
				BufferedInputStream input = new BufferedInputStream(ConnectionHandler.this.connection.getInputStream());
				
				while(!ConnectionHandler.this.connection.isClosed()){
					ObjectInputStream ois = new ObjectInputStream(input);
					Request request;
					request = (Request)ois.readObject();
					if(request == null){
						tryCloseConnection();
					}else{
						try{
							ConnectionHandler.this.dispatcher.handleRequest(request, ConnectionHandler.this);
						}catch(InterruptedException e){
							tryCloseConnection();
                            return;
						} catch (ExecutionException e) {
							tryCloseConnection();
                            return;
						}
					}
				}
			}catch(IOException e){
				tryCloseConnection();
			}catch(ClassNotFoundException e){
				tryCloseConnection();
			}
		}
	}
	
	private class OutgoingHandler implements Runnable{
		private final Response response;
		public OutgoingHandler(Response response) {
			this.response =  response;
		}
		
		@Override
		public void run(){
			try {
				BufferedOutputStream output = new BufferedOutputStream(
						ConnectionHandler.this.connection.getOutputStream());
				ObjectOutputStream oos = new ObjectOutputStream(output);
				oos.writeObject(response);
				oos.flush();
			} catch (IOException e) {
				tryCloseConnection();
			}
		}
	}

	ConnectionHandler(Socket connection, RequestDispatcher dispatcher, ExecutorService outgoingHandlerPool) {
		this.connection = connection;
		this.dispatcher = dispatcher;
		this.outgoingHandlerPool = outgoingHandlerPool;
	}

	public static ConnectionHandler create(Socket connection,
			ExecutorService requestHandlerPool,
			ExecutorService outgoingHandlerPool,
			ImplementationContainer implContainer) {
		RequestDispatcher dispatcher = new RequestDispatcher(requestHandlerPool, implContainer);
		return new ConnectionHandler(connection, dispatcher, outgoingHandlerPool);
	}

	public void closeConnection() {
		tryCloseConnection();
	}
	
	private void tryCloseConnection() {
        try {
            connection.close();
        } catch (IOException e) {
            // Assume connection is closed.
        }
    }
	
	public Runnable createIncomingHandler() {
		return new IncomingHandler();
	}
	
	public Runnable createOutgoingHandler(Response response) {
		return new OutgoingHandler(response);
	}
	
	public ExecutorService getOutgoingHandlerPool(){
		return this.outgoingHandlerPool;
	}
}
