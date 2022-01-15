package com.colorfulwolf.breakoutbox;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;

public class BobCommand {

	private final static int ACCESS_CACHE_SIZE = 1023;
	@SuppressWarnings("serial")
	private final Map<String, Long> _accessCache = new LinkedHashMap<String, Long>(ACCESS_CACHE_SIZE + 1, .75F, true) {
	    public boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
	        return size() > ACCESS_CACHE_SIZE;
	    }
	};
	private final Map<String, Long> accessCache = Collections.synchronizedMap(_accessCache);

	@SuppressWarnings("serial")
	private final Map<BlockPos, Integer> lastResultMap = new LinkedHashMap<BlockPos, Integer>(ACCESS_CACHE_SIZE + 1, .75F, true) {
	    public boolean removeEldestEntry(Map.Entry<BlockPos, Integer> eldest) {
	        return size() > ACCESS_CACHE_SIZE;
	    }
	};

	public String path;
	public boolean parseOutput = false;
	public boolean verbose = false; // If set to true, output additional log lines

	public boolean runAsRegularPlayer = false;
	public boolean runAsCommandBlock = true;
	public boolean runAsOpPlayer = true;

	public long commandTimeoutMilliseconds = 5000;

	public long globalRateLimitMilliseconds = 500; // Limit any call to this command to running this only once every 500ms
	public long blockRateLimitMilliseconds = 500; // Limit any call to this command to running this only once every 500ms

	public void parse(String key, String value) {
		if (key.equals("path")) {
			this.path = value;
		} else if (key.equals("commandTimeoutMilliseconds")) {
			this.commandTimeoutMilliseconds = Long.parseLong(value);
		} else if (key.equals("globalRateLimitMilliseconds")) {
			this.globalRateLimitMilliseconds = Long.parseLong(value);
		} else if (key.equals("blockRateLimitMilliseconds")) {
			this.blockRateLimitMilliseconds = Long.parseLong(value);
		} else if (key.equals("parseOutput")) {
			this.parseOutput = value.equalsIgnoreCase("true");
		} else if (key.equals("verbose")) {
			this.verbose = value.equalsIgnoreCase("true");
		} else if (key.equals("runAsCommandBlock")) {
			this.runAsCommandBlock = value.equalsIgnoreCase("true");
		} else if (key.equals("runAsRegularPlayer")) {
			this.runAsRegularPlayer = value.equalsIgnoreCase("true");
		} else if (key.equals("runAsOpPlayer")) {
			this.runAsOpPlayer = value.equalsIgnoreCase("true");
		}
	}

	public boolean callAllowed(int x, int y, int z) {
		long currentTime = System.currentTimeMillis();
		String globalCacheKey = this.path;
		Long lastGlobalAccessTime = this.accessCache.get(globalCacheKey);
		if (lastGlobalAccessTime != null && currentTime - lastGlobalAccessTime < this.globalRateLimitMilliseconds) {
			return false;
		}
		String localCacheKey = x + "," + y + "," + z + "," + this.path;
		Long lastLocalAccessTime = this.accessCache.get(localCacheKey);
		if (lastLocalAccessTime != null && currentTime - lastLocalAccessTime < this.blockRateLimitMilliseconds) {
			return false;
		}
		this.accessCache.put(localCacheKey, currentTime);
		this.accessCache.put(globalCacheKey, currentTime);
		return true;
	}

	public void setLastResult(BlockPos pos, int val) {
		this.lastResultMap.put(pos,  val);
	}
	
	public Integer lastResult(BlockPos pos) {
		return this.lastResultMap.get(pos);
	}
	
	@Override
	public String toString() {
		String flips = this.parseOutput + "," + this.runAsCommandBlock + "," + this.runAsRegularPlayer + ","
				+ this.runAsOpPlayer;
		return "CommandOptions(" + flips + "," + this.path + ")";
	}
}
