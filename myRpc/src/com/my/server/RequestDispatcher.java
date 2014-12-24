package com.my.server;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.my.shared.InvokeMethodRequest;
import com.my.shared.InvokeMethodResponse;
import com.my.shared.InvokeMethodResponse.Type;
import com.my.shared.Request;
import com.my.shared.Response;

public class RequestDispatcher {
	private static final Logger logger = Logger.getLogger(RequestDispatcher.class.getName());
	private final ImplementationContainer container;
	/** A pool that can be shared among all dispatchers. */
	// 本来我以为pool可以在dispatcher里面生成的, 但是那样太浪费了, 如果在Server初始化的时候
	// 生成线程池, 然后传给给个模块进行使用, 那么线程池的作用会大的多, 而不是各个模块下的每个子任务来生成线程池
	private final ExecutorService requestHandlerPool;
	
	private interface ArrayUtil{
		String toString(Object[] arr);
	}
	
	private class RequestHandler implements Runnable{
		private final InvokeMethodRequest request;
		private final ImplementationContainer container;
		private final ConnectionHandler connectionHandler;
		
		public RequestHandler(Request request,
				ImplementationContainer container,
				ConnectionHandler connectionHandler){
			this.request = (InvokeMethodRequest) request;
			this.container = container;
			this.connectionHandler = connectionHandler;
		}
		
		public Response internalRun() throws Exception {
			Object implementation = container
					.getImplementation(request.getInterfaceName());
			if (implementation == null)
				throw new RuntimeException("Internal Error: No impl found");
			Method method = findMethod(implementation);
			Object[] arguments = request.getArgumentsAsArray();
			Object res = method.invoke(implementation, arguments);
			if (logger.isLoggable(Level.FINER)) {
				ArrayUtil au = args -> {
					StringBuilder sb = new StringBuilder();
					if(args != null && args.length > 0){
						for(Object obj : args){
							sb.append(obj);
						}
					}
					return sb.toString();
				};
                logger.fine(String.format("%s(%s)",
                        method.getName(), au.toString(arguments)));
            }
			return new InvokeMethodResponse(res, null,
					Type.OK);
		}
		
		private InvokeMethodResponse createExceptionResponse(Exception e) {
			InvokeMethodResponse failedResponse = new InvokeMethodResponse(null,
					e.toString(), Type.EXCEPTION);
			return failedResponse;
		}

		private Method findMethod(Object implementation)
				throws NoSuchMethodException, SecurityException {
			Class<?>[] paramTypes = request.getParameterTypesAsArray();
			String methodName = request.getMethodName();
			Method method = implementation.getClass().getMethod(methodName,
					paramTypes);
			return method;
		}
		
		@Override
		public void run(){
			Response response = null;
			try{
				response = internalRun();
			}catch(Exception e){
				response = createExceptionResponse(e);
			}
			Runnable run = connectionHandler.createOutgoingHandler(response);
			connectionHandler.getOutgoingHandlerPool().execute(run);
		}
	}
	
	public RequestDispatcher(ExecutorService requestHandlerPool,
			ImplementationContainer implContainer) {
		this.requestHandlerPool = requestHandlerPool;
		this.container = implContainer;
	}

	/**
	 * 函数块里面并没有抛出InterruptedException, 这个是为了在程序运行时如果出现该异常, 而又想在调用他的
	 * 函数里捕获该异常的情况在故意抛出的
	 * @param request
	 * @throws InterruptedException
	 * @throws ExecutionException 
	 */
	public void handleRequest (Request request, ConnectionHandler connectionHandler) throws InterruptedException, ExecutionException {
		RequestHandler handler = new RequestHandler(request, container, connectionHandler);
		requestHandlerPool.execute(handler);
	}

}
