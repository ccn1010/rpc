package com.my.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.my.exception.SimpleRpcException;
import com.remote.Minus;
import com.remote.Remote;

public class Client {
	private final String host;
	private final int port;
	private ConnectionHandler handler;

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
		handler = createConnectionHandler();
	}

	private ConnectionHandler createConnectionHandler() {
		ConnectionHandler handler = new ConnectionHandler(host, port);
		try {
			handler.connect();
		} catch (Exception e) {
			throw new SimpleRpcException(e);
		}
		return handler;
	}

	@SuppressWarnings("unchecked")
	public <T> T getImplementation(Class<T> interfaceClazz) {
		InvocationHandler invocationHandler = new ClientInvocationHandler(
				handler, interfaceClazz);
		T proxy = ((T) Proxy.newProxyInstance(Client.class.getClassLoader(),
				new Class[] { interfaceClazz }, invocationHandler));
		return proxy;
	}

	public void disconnect() {
		handler.close();
	}

	public static void main(String[] args) {
		int port = 5010;
		Client rmiClient = new Client("localhost", port);
		Remote clientProxy = rmiClient.getImplementation(Remote.class);
		String r = clientProxy.method("aa", "11");
		System.out.println(r);
		r = clientProxy.method("2", "4", "6");
		System.out.println(r);
		Minus minus = rmiClient.getImplementation(Minus.class);
		int a = minus.method(8, 2);
		System.out.println(a);
		rmiClient.disconnect();
	}

	public static void mains(String[] args) throws Exception {
		ConnectionHandler handler = new ConnectionHandler("localhost", 5010);
		handler.connect();

		boolean isClosed = false;
		ClientInvocationHandler invocation = new ClientInvocationHandler(
				handler, Remote.class);
		Remote remote = (Remote) Proxy.newProxyInstance(
				Client.class.getClassLoader(), new Class[] { Remote.class },
				invocation);
		while (!isClosed) {
			String res = remote.method("hello");
			System.out.println("============ " + res);
		}
		System.out.println("socket over");
		// writer.close();
		// reader.close();
		// socket.close();
	}
}
