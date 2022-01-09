package com.colorfulwolf.breakoutbox;

public class BobCommand {

	public String path;
	public boolean parseOutput = false;

	public boolean runAsCommandBlock = true;
	public boolean runAsRegularPlayer = true;
	public boolean runAsOpPlayer = true;

	public long commandTimeoutMilliseconds = 5000;
	public long commandRateLimitMilliseconds = 500; // Limit each command to running this only once every 500ms

	public void parse(String key, String value) {
		if (key.equals("path")) {
			this.path = value;
		} else if (key.equals("commandTimeoutMilliseconds")) {
			this.commandTimeoutMilliseconds = Long.parseLong(value);
		} else if (key.equals("commandRateLimitMilliseconds")) {
			this.commandRateLimitMilliseconds = Long.parseLong(value);
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

	@Override
	public String toString() {
		String flips = this.parseOutput + "," + this.runAsCommandBlock + "," + this.runAsRegularPlayer + ","
				+ this.runAsOpPlayer;
		return "CommandOptions(" + flips + "," + this.path + ")";
	}
}
