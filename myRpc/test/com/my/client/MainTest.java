package com.my.client;

public class MainTest {
	public void throwException(){
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(5*1000);
					Integer.parseInt("aaa");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		t.start();
		System.out.println("main thread end!");
	}
	
	public static void main(String[] args) {
		MainTest test = new MainTest();
		test.throwException();
	}
}
