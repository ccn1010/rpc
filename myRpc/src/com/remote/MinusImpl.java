package com.remote;

public class MinusImpl implements Minus {

	@Override
	public int method(int num0, int num1) {
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return num0 - num1;
	}

}
