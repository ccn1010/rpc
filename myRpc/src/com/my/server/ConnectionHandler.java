package com.my.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import com.my.shared.Request;
import com.my.shared.Response;

public class ConnectionHandler {
	private final Socket connection;
	private final BlockingQueue<Response> dispatcherOutput;
	private final RequestDispatcher dispatcher;
	
	private class IncomingHandler implements Runnable{
		@Override
		public void run(){
			try{
				BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
				
				while(!connection.isClosed()){
					ObjectInputStream ois = new ObjectInputStream(input);
					Request request;
					request = (Request)ois.readObject();
					if(request == null){
						tryCloseConnection();
					}else{
						try{
							dispatcher.handleRequest(request);
						}catch(InterruptedException e){
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
		@Override
		public void run(){
			try {
				BufferedOutputStream output = new BufferedOutputStream(
						connection.getOutputStream());
				while(!connection.isClosed()){
					ObjectOutputStream oos = new ObjectOutputStream(output);
					oos.writeObject(dispatcherOutput.take());
					oos.flush();
				}
			} catch (IOException e) {
				tryCloseConnection();
			} catch(InterruptedException e){
				tryCloseConnection();
			}
		}
	}

	ConnectionHandler(Socket connection, RequestDispatcher dispatcher,
			BlockingQueue<Response> dispatcherOutput) {
		this.connection = connection;
		this.dispatcher = dispatcher;
		this.dispatcherOutput = dispatcherOutput;
	}

	public static ConnectionHandler create(Socket connection,
			ExecutorService requestHandlerPool,
			ImplementationContainer implContainer) {
		BlockingQueue<Response> dispatcherOutput =
				new ArrayBlockingQueue<Response>(RequestDispatcher.DEFAULT_QUEUE_SIZE);
		RequestDispatcher dispatcher = new RequestDispatcher(requestHandlerPool, dispatcherOutput, implContainer);
		return new ConnectionHandler(connection, dispatcher, dispatcherOutput); 
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

	public Runnable createOutgoingHandler() {
		return new OutgoingHandler();
	}
}
