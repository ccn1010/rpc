package com.my.server.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import com.my.shared.InvokeMethodRequest;
import com.my.shared.Request;
import com.my.shared.Response;

public class ConnectionHandler {
	private final Socket socket;
	private final ImplementationContainer container;
	private Thread handlingThread;

	public ConnectionHandler(Socket newSocket,
			ImplementationContainer container) {
		this.socket = newSocket;
		this.container = container;
	}
	
	public void start(){
		handlingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("SingleConnection Thread Running");
				handleConnection();
			}
		});
		handlingThread.start();
	}
	
	private void handleConnection() {
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			while( !Thread.currentThread().isInterrupted() ){
                readRequestAndSendResponse( is, os );
            }
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void readRequestAndSendResponse(InputStream is, OutputStream os) {
		Request request;
		try {
			request = readRequest(is);
			Response response = processRequest(request);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(response);
			oos.flush();
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	private Request readRequest(InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(is);
		Request request = (Request)ois.readObject();
		return request;
	}

	private Response processRequest(Request request) {
		InvokeMethodRequest req = (InvokeMethodRequest)request;
		InvokeMethodProcessor processor = new InvokeMethodProcessor(container, req);
		return processor.process();
	}
	
	public void stop(){
        handlingThread.interrupt();
    }

	public Runnable createIncomingHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	public static ConnectionHandler create(Socket connection,
			ExecutorService requestHandlerPool,
			ImplementationContainer implContainer) {
		// TODO Auto-generated method stub
		return null;
	}
}
