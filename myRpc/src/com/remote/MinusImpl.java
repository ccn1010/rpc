package com.remote;

import java.io.Serializable;

public class MinusImpl implements Minus, Serializable {

	private static final long serialVersionUID = 1L;

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
