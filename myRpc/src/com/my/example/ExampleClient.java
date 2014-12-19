package com.my.example;

import com.my.client.Client;
import com.remote.Minus;
import com.remote.Remote;

public class ExampleClient {
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
}
