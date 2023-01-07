package com.shareE.forum;

import java.io.IOException;

public class WkTests {

	public static void main(String[] args) {
		String cmd = "/usr/local/bin/wkhtmltoimage --quality 75 https://www.google.com /Users/yvette/Documents/Programming/Project/ShareE/wk-images/3.png";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			if (p.waitFor() == 0) {
				System.out.println("OK");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
