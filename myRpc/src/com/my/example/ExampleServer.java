package com.my.example;

import com.my.server.Server;
import com.remote.Minus;
import com.remote.MinusImpl;
import com.remote.Remote;
import com.remote.RemoteImpl;

public class ExampleServer {

	public static void main(String[] args) {
		int port = 5010;
		Server server = Server.create(port);
		Remote remote = new RemoteImpl();
		Minus minus = new MinusImpl();
		server.addImplementation(Remote.class, remote);
		server.addImplementation(Minus.class, minus);
		server.start();
		System.out.println("Running server on port " + server.getPort());
		try {
			server.join();
		} catch (InterruptedException e) {
		}
	}

}
