package com.remote;

public class RemoteImpl implements Remote {
	public String mes;
	
	@Override
	public String method(String str){
		System.out.println(str);
		return "success: "+str;
	}
	
	@Override
	public void method(){
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(mes);
	}

	@Override
	public String method(String s1, String s2) {
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return s1 + ": " +s2;
	}
	
	@Override
	public String method(String s1, String s2, String s3){
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return s1 + " + "+s2+" = "+s3;
	}
}
