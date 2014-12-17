package com.my.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.my.exception.SimpleRpcRemoteException;
import com.my.shared.InvokeMethodRequest;
import com.my.shared.InvokeMethodResponse;

public class ClientInvocationHandler implements InvocationHandler {
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
			throw new SimpleRpcRemoteException( response.getExceptionToString() );
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
