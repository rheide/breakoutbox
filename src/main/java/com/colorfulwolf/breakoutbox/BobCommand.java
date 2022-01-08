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
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.CommandBlock;

public class BobCommand implements Command<CommandSourceStack> {

	private MinecraftServer server;
	private final ThreadPoolExecutor executor;
	
	private static final Logger LOGGER = LogManager.getLogger();
	private BobOptions options;
	private Map<String, BobCommandOptions> commandOptions;
	private int commandTimeoutSeconds;

	public BobCommand(MinecraftServer server, BobOptions options, Map<String, BobCommandOptions> commandOptions) {
		this.server = server;
		this.options = options;
		this.commandOptions = commandOptions;
		this.executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(options.commandThreads);
	}

	@Override
	public int run(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		LOGGER.info("Queueing new command");
		// Parse arguments here, return error if poop
		try {
			Collection<? extends Entity> entities = EntityArgument.getEntities(command, "targets");
			LOGGER.info("entities: " + entities);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Entity entity = command.getSource().getEntity();
		// TODO check if player entity, if op
		// TODO check if command block
		
		//ParseResults<CommandSourceStack> parseResults = this.server.getCommands().getDispatcher().parse("data modify block 361 71 -92 SuccessCount set value 4", command.getSource());
		String cmd = command.getArgument("cmd", String.class);
		if (this.commandOptions.containsKey(cmd)) {
			BobCommandOptions commandOptions = this.commandOptions.get(cmd);
			// TODO rate limiting here
			//this.executor.execute(new BobExternalCommandTask(this.server, this.options, commandOptions, command, parseResults));
			this.server.execute(new BobExternalCommandTask(this.server, this.options, commandOptions, command, null));
			
		} else if (entity != null) {
			entity.sendMessage(new TextComponent("Command not found: " + cmd), Constants.ID);
		}		
		// TODO does command block state change need to be undone here?
		LOGGER.info("Parsing finished");
		
		return 0;
	}
	
}
