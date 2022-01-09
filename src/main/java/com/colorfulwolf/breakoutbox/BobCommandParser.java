package com.colorfulwolf.breakoutbox;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

public class BobCommandParser implements Command<CommandSourceStack> {

	private MinecraftServer server;

	private static final Logger LOGGER = LogManager.getLogger();
	private Map<String, BobCommand> commandOptions;

	public BobCommandParser(MinecraftServer server, Map<String, BobCommand> commandOptions) {
		this.server = server;
		this.commandOptions = commandOptions;
	}

	@Override
	public int run(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		LOGGER.info("Queueing new command");
		
		Entity entity = command.getSource().getEntity();
		LOGGER.info("Entity: " + command.getSource() + " - " + entity);

		String cmd = command.getArgument("cmd", String.class);
		
		if (!this.commandOptions.containsKey(cmd)) {
			if (entity != null) {
				entity.sendMessage(new TextComponent("Command not found: " + cmd), Constants.ID);
			}
			return 0;
		}

		BobCommand commandOptions = this.commandOptions.get(cmd);
		
		boolean canRun = false;
		if (entity == null) {
			// A command block is running this
			canRun = commandOptions.runAsCommandBlock;
		} else {
			// A player is running this
			canRun = commandOptions.runAsRegularPlayer || command.getSource().hasPermission(Constants.PERM_OP);
		}
		
		LOGGER.info("Command source: " + command.getSource() + " PERM: " + command.getSource().hasPermission(Constants.PERM_OP));

		if (canRun) {
			// TODO rate limiting here
			this.server.execute(new BobExternalCommandTask(commandOptions, command));
		} else if (entity != null) {
			entity.sendMessage(new TextComponent("User not allowed to run command: " + cmd), Constants.ID);
		}
		return 0; // Always return zero because the finished task will update the state
	}

}
