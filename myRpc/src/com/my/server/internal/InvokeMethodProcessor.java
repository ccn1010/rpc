package com.my.server.internal;

import java.lang.reflect.Method;

import com.my.shared.InvokeMethodRequest;
import com.my.shared.InvokeMethodResponse;
import com.my.shared.InvokeMethodResponse.Type;
import com.my.shared.Response;

public class InvokeMethodProcessor {
	private final InvokeMethodRequest invokeMethodRequest;
	private final ImplementationContainer implementationContainer;

	public InvokeMethodProcessor(ImplementationContainer container,
			InvokeMethodRequest req) {
		this.invokeMethodRequest = req;
		this.implementationContainer = container;
	}

	public Response process() {
		System.out.println("Processor is processing!!");
		try {
			Object implementation = implementationContainer
					.getImplementation(invokeMethodRequest.getInterfaceName());
			if (implementation == null)
				throw new RuntimeException("Internal Error: No impl found");
			Method method = findMethod(implementation);
			Object[] arguments = invokeMethodRequest.getArgumentsAsArray();
			Object res = method.invoke(implementation, arguments);
			InvokeMethodResponse response = new InvokeMethodResponse(res, null,
					Type.OK);
			return response;
		} catch (Exception e) {
			return createExceptionResponse(e);
		}
	}

	private InvokeMethodResponse createExceptionResponse(Exception e) {
		InvokeMethodResponse failedResponse = new InvokeMethodResponse(null,
				e.toString(), Type.EXCEPTION);
		return failedResponse;
	}

	private Method findMethod(Object implementation)
			throws NoSuchMethodException, SecurityException {
		Class<?>[] paramTypes = invokeMethodRequest.getParameterTypesAsArray();
		String methodName = invokeMethodRequest.getMethodName();
		Method method = implementation.getClass().getMethod(methodName,
				paramTypes);
		return method;
	}

}
