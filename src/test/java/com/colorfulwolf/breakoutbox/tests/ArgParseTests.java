package com.colorfulwolf.breakoutbox.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.colorfulwolf.breakoutbox.BobExternalCommandTask;

class ArgParseTests {

	@Test
	void testSimpleSubstitution() {
		String literalPath = "python -v $args";
		Map<String,String> vars = new HashMap<String, String>();
		vars.put("$args", "first second");
		List<String> res = BobExternalCommandTask.parseArguments(literalPath, vars);
		assertEquals(
			String.join(" # ", new String[] {
				"python",
				"-v",
				"first",
				"second"
			}),
			String.join(" # ", res)
		);
	}

	@Test
	void testQuotePartInPath() {
		String literalPath = "python -v \"some long path\"";
		Map<String,String> vars = new HashMap<String, String>();
		List<String> res = BobExternalCommandTask.parseArguments(literalPath, vars);
		assertEquals(
			String.join(" # ", new String[] {
				"python",
				"-v",
				"some long path",
			}),
			String.join(" # ", res)
		);
	}

	@Test
	void testQuotePartInSubstitution() {
		String literalPath = "python -v \"$args\"";
		Map<String,String> vars = new HashMap<String, String>();
		vars.put("$args", "\"a player name\" and some text");
		List<String> res = BobExternalCommandTask.parseArguments(literalPath, vars);
		assertEquals(
			String.join(" # ", new String[] {
				"python",
				"-v",
				"a player name",
				"and",
				"some",
				"text",
			}),
			String.join(" # ", res)
		);
	}

	@Test
	void testTurnoffLight() throws InterruptedException, IOException {
		String literalPath = "C:\\Python38\\python.exe C:\\Games\\minecraftserver\\homeassistant.py $args";
		Map<String,String> vars = new HashMap<String, String>();
		vars.put("$args", "toggle light.study_lamp");
		List<String> cmdArgs = BobExternalCommandTask.parseArguments(literalPath, vars);

		assertEquals(
			String.join(" # ", new String[] {
				"C:\\Python38\\python.exe",
				"C:\\Games\\minecraftserver\\homeassistant.py",
				"toggle",
				"light.study_lamp",
			}),
			String.join(" # ", cmdArgs)
		);
/*
		ProcessBuilder builder = new ProcessBuilder(cmdArgs);
		Process process = builder.start();
		if (!process.waitFor(5000, TimeUnit.MILLISECONDS)) {
			process.destroy();
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		int exitVal = Math.max(Math.min(process.exitValue(), 15), 0);
		assertEquals(1, exitVal);*/
	}
}
