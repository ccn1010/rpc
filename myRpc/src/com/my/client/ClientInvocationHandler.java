package com.my.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.my.shared.InvokeMethodRequest;
import com.my.shared.InvokeMethodResponse;

public class ClientInvocationHandler implements InvocationHandler {
	private static final Logger logger = Logger.getLogger(ClientInvocationHandler.class.getName());
	private final ConnectionHandler connectionHandler;
	private final Class<?> interfaceClazz;

	public ClientInvocationHandler(ConnectionHandler connectionHandler,
			Class<?> interfaceClazz) {
		this.connectionHandler = connectionHandler;
		this.interfaceClazz = interfaceClazz;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		InvokeMethodRequest request = createRequest(method, args);
		InvokeMethodResponse response = (InvokeMethodResponse) connectionHandler.sendRequest(request);
		switch (response.getType()) {
		case OK:
			return response.getReturnValue();
		case EXCEPTION:
			logger.log(Level.WARNING, response.getExceptionToString());
			throw new Exception( response.getExceptionToString() );
		default:
			throw new RuntimeException();
		}
	}

	public InvokeMethodRequest createRequest(Method method, Object[] args) {
		List<Object> params = Arrays.asList(args);
		List<Class<?>> requestTypes = Arrays.asList(method.getParameterTypes());
		InvokeMethodRequest request = new InvokeMethodRequest(
				interfaceClazz.getName(), method.getName(), params,
				requestTypes);
		return request;
	}
}
