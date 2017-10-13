package com.xwtz.platform.dealing;

import java.io.File;

public class Test {
	public static void main(String[] args) throws Exception {
		File file = new File("D:/deal_out.xls");
		if (!file.exists()) {
			file.createNewFile();
		}
	}
}
