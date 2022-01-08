package com.colorfulwolf.breakoutbox;

import java.io.PrintWriter;

public class BobCommandOptions {

	public String path;
	public String args = "";
	public boolean parseOutput = false;

	public boolean runAsCommandBlock = true;
	public boolean runAsRegularPlayer = true;
	public boolean runAsOpPlayer = true;

	public void parse(String key, String value) {
		if (key.equals("path")) {
			this.path = value;
		} else if (key.equals("args")) {
			this.args = value;
		} else if (key.equals("parseOutput")) {
			this.parseOutput = value.equalsIgnoreCase("true");
		} else if (key.equals("runAsCommandBlock")) {
			this.runAsCommandBlock = value.equalsIgnoreCase("true");
		} else if (key.equals("runAsRegularPlayer")) {
			this.runAsRegularPlayer = value.equalsIgnoreCase("true");
		} else if (key.equals("runAsOpPlayer")) {
			this.runAsOpPlayer = value.equalsIgnoreCase("true");
		}
	}

	public void write(PrintWriter pw) {
		pw.println("path=" + this.path);
		pw.println("args=" + this.args);
		pw.println("parseOutput=" + this.parseOutput);
		pw.println("runAsCommandBlock=" + this.runAsCommandBlock);
		pw.println("runAsRegularPlayer=" + this.runAsRegularPlayer);
		pw.println("runAsOpPlayer=" + this.runAsOpPlayer);
	}

	@Override
	public String toString() {
		String flips = this.parseOutput + "," + this.runAsCommandBlock + "," + this.runAsRegularPlayer + ","
				+ this.runAsOpPlayer;
		return "CommandOptions(" + flips + "," + this.path + " " + this.args + ")";
	}
}
