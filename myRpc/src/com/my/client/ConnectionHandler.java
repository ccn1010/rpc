package com.my.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.my.shared.InvokeMethodRequest;
import com.my.shared.Request;
import com.my.shared.Response;

public class ConnectionHandler {
	private final String host;
    private final int port;
    private Socket socket;
    private OutputStream os;
    private InputStream is;

    public ConnectionHandler( String host, int port ){
        this.host = host;
        this.port = port;
    }

    public void connect() throws UnknownHostException, IOException{
        socket = new Socket( host, port );
        os = socket.getOutputStream();
        is = socket.getInputStream();
    }

    public Response sendRequest( Request request ) throws Exception{
    	InvokeMethodRequest req = (InvokeMethodRequest)request;
    	ObjectOutputStream oos = new ObjectOutputStream(os);
    	oos.writeObject(req);
    	oos.flush();
    	ObjectInputStream ois = new ObjectInputStream(is);
    	Response response = (Response)ois.readObject();
    	return response;
    }
    
    public void close(){
        try{
            is.close();
            os.close();
            socket.close();
        }
        catch( IOException e ){
        }
    }
}
