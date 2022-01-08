package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bleh {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("TEST");
		System.out.println(UUID.randomUUID().toString());

		List<String> list = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher("C:\\Python38\\python.exe C:\\workspace\\dostuff.py --bleh --blah $abc $def");
		while (m.find())
		    list.add(m.group(1).replace("\"", ""));

		System.out.println(list);
		
		//ProcessBuilder builder = new ProcessBuilder("C:\\workspace\\venv\\Scripts\\python.exe", "dostuff.py");
		ProcessBuilder builder = new ProcessBuilder(list);
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
	    System.out.println("DONE");
	    
	}

}
