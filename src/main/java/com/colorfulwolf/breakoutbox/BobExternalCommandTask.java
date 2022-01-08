package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class BobExternalCommandTask implements Runnable {

	// private CommandContext<CommandSourceStack> command;
	private BobCommandOptions cmd;

	private MinecraftServer server;

	private static final Logger LOGGER = LogManager.getLogger();

	private BobOptions options;

	private CommandContext<CommandSourceStack> command;

	private ParseResults<CommandSourceStack> parseResults;

	public BobExternalCommandTask(MinecraftServer server, BobOptions options, BobCommandOptions cmd,
			CommandContext<CommandSourceStack> command, ParseResults<CommandSourceStack> parseResults) {
		this.server = server;
		this.options = options;
		this.cmd = cmd;
		this.command = command;
		this.parseResults = parseResults;
	}

	@Override
	public void run() {
		/*
		 * try { Collection<? extends Entity> entities =
		 * EntityArgument.getEntities(this.command, "targets"); LOGGER.info("entities: "
		 * + entities); } catch (Exception e) { e.printStackTrace(); }
		 * 
		 * LOGGER.info("/bob command dispatched 123");
		 * 
		 * LOGGER.info("Input: " + this.command.getInput()); // full command text
		 * LOGGER.info("Source pos: " + this.command.getSource().getPosition()); //
		 * Position of command block or player // typing the cmd
		 * 
		 * // null (because command block?) // or Entity:
		 * ServerPlayer['RandyRanger'/169, l='ServerLevel[world]', x=355.43, // y=71.00,
		 * z=-91.34] LOGGER.info("Entity: " + this.command.getSource().getEntity());
		 * 
		 * LOGGER.info("Root: " + this.command.getRootNode().getName());
		 * 
		 * LOGGER.info("LAST CHILD ENTITY: " +
		 * this.command.getLastChild().getSource().getEntity()); //
		 * LOGGER.info("ROOT ENTITY: " + command.getRootNode().);
		 * 
		 * List<ParsedCommandNode<CommandSourceStack>> nodes = this.command.getNodes();
		 * for (ParsedCommandNode<CommandSourceStack> node : nodes) {
		 * LOGGER.info("Node: " + node); }
		 * 
		 * try { LOGGER.info("PLAYER: " +
		 * this.command.getSource().getPlayerOrException()); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */

		LOGGER.info("Executing " + this.cmd.path + " " + this.cmd.args);
		/*String[] cmdBits = this.cmd.path.split("(?<=\") *(?=\")");
		for (int i=0; i<cmdBits.length; i++) {
			if (cmdBits[i].startsWith("\"")) {
				cmdBits[i] = cmdBits[i].substring(1);
				if (cmdBits[i].endsWith("\"")) {
					cmdBits[i] = cmdBits[i].substring(0, cmdBits[i].length() - 2);
				}
			}
		}
		LOGGER.info(Arrays.toString(cmdBits));*/
		
		// This is stupid... why is this so hard in java???
		List<String> cmdArgs = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(this.cmd.path);
		while (m.find())
			cmdArgs.add(m.group(1).replace("\"", ""));
		
		ProcessBuilder builder = new ProcessBuilder(cmdArgs);
		// "C:\\Python38\\python.exe",// "C:\\workspace\\dostuff.py");
		Process process = null;
		try {
			process = builder.start();
			if (!process.waitFor(this.options.commandTimeoutSeconds, TimeUnit.SECONDS)) {
				LOGGER.info("TIMED OUT. KILLING");
				process.destroy();
			} else {
				StringBuilder out = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					out.append(line);
					out.append("\n");
				}
				LOGGER.info(out);

				int exitVal = process.exitValue();
				LOGGER.info("Exit val: " + exitVal);
				exitVal = Math.min(exitVal, 15);
				exitVal = Math.max(exitVal, 0);
				// TODO keep track of time when executing returned commands
				// TODO scoreboard variables
				// this.server.getCommands().getDispatcher().execute("say command finished",
				// command.getSource());
				this.command.getSource().getPosition();
				
				
				//this.command.getSource().getLevel().getBlo
				
				//blockState.setValue(IntegerProperty.create("SuccessCount", 0, 100), exitVal);
				
				/*Vec3 pos = this.command.getSource().getServer().getCommands().getDispatcher().
				BlockState blockState = this.command.getSource().getLevel().getBlockState(new BlockPos(pos));
				LOGGER.info("State: " + blockState);
				LOGGER.info("Block: " + blockState.getBlock());*/
				
				// Output @modified block data message to player
				//this.server.getCommands().getDispatcher().execute("data modify block 361 71 -92 SuccessCount set value " + exitVal, this.command.getSource());

				this.server.getCommands().performCommand(this.command.getSource(), "data modify block 361 71 -92 SuccessCount set value " + exitVal);
				
				//BaseCommandBlock block = blockState.getBlock();
				//block.setSuccessCount(exitVal);
				// ((BlockCommandSender)
				// sender).getBlock().getState().setMetadata("SuccessCount", new
				// MetadataValueOutput(state));
				// ((BlockCommandSender) sender).getBlock().getState().update();
			}
		} catch (InterruptedException e) {
			LOGGER.info("INTERRUPTED. KILLING");
			if (process != null) {
				process.destroy();
			}
		} catch (Exception e) {
			LOGGER.info("EXC: " + e);
			e.printStackTrace();
			if (process != null) {
				process.destroy();
			}
		}
	}
}
