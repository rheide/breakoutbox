package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class BobCommand implements Command<CommandSourceStack> {

	private MinecraftServer server;
	private final ThreadPoolExecutor executor;
	
	private static final Logger LOGGER = LogManager.getLogger();
	private BobOptions options;
	private Map<String, BobCommandOptions> commandOptions;

	public BobCommand(MinecraftServer server, BobOptions options, Map<String, BobCommandOptions> commandOptions) {
		this.server = server;
		this.options = options;
		this.commandOptions = commandOptions;
		this.executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(options.commandThreads);
	}

	@Override
	public int run(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		LOGGER.info("Queueing new command");
		this.executor.execute(new BobExternalCommandTask(this.server, command));
		// TODO does command block state change need to be undone here?
		LOGGER.info("Parsing finished");
		return 0;
	}
	
}
