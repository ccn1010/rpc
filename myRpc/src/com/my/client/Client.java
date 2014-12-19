package com.my.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

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
			throw new RuntimeException(e);
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
}
