package com.remote;

public interface Remote {
	String method(String str);
	void method();
	
	String method(String s1, String s2);
	public String method(String s1, String s2, String s3);
}
