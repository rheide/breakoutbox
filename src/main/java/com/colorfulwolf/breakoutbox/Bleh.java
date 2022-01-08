package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class Bleh {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("TEST");
		System.out.println(UUID.randomUUID().toString());
		String[] line = "a=b=c=d".split("=", 2);
		for (String s: line) {
			System.out.println(s);
		}
		
		/*ProcessBuilder builder = new ProcessBuilder("C:\\workspace\\venv\\Scripts\\python.exe", "dostuff.py");
	    Process process = builder.start();
	    StringBuilder out = new StringBuilder();
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	        String line = null;
	      while ((line = reader.readLine()) != null) {
	        out.append(line);
	        out.append("\n");
	      }
	      System.out.println(out);
	    }
	    System.out.println("DONE");*/
	    
	}

}
