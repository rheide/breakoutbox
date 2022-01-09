package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

public class BobExternalCommandTask implements Runnable {

	private BobCommand cmd;

	private MinecraftServer server;

	private static final Logger LOGGER = LogManager.getLogger();

	private CommandContext<CommandSourceStack> command;

	public BobExternalCommandTask(BobCommand cmd, CommandContext<CommandSourceStack> command) {
		this.server = command.getSource().getServer();
		this.cmd = cmd;
		this.command = command;
	}

	private String entityToString(Entity e) {
		LOGGER.info(e + " - " + e.getClass());
		return e.getName().getContents() + "," + e.getBlockX() + "," + e.getBlockY() + "," + e.getBlockZ();
	}

	@Override
	public void run() {
		LOGGER.info("Executing " + this.cmd.path);

		Process process = null;
		try {
			Map<String, String> vars = getVars();

			// This is stupid... why is escaping a string so hard in java???
			List<String> cmdArgs = new ArrayList<String>();
			Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(this.cmd.path);
			while (m.find()) {
				String cmdArg = m.group(1).replace("\"", "");
				for (String var : vars.keySet()) {
					cmdArg = cmdArg.replace(var, vars.get(var));
				}
				cmdArgs.add(cmdArg);
			}

			LOGGER.info(String.join(" ", cmdArgs));
			ProcessBuilder builder = new ProcessBuilder(cmdArgs);

			process = builder.start();
			if (!process.waitFor(this.cmd.commandTimeoutMilliseconds, TimeUnit.MILLISECONDS)) {
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

				int exitVal = Math.max(Math.min(process.exitValue(), 15), 0);
				LOGGER.info("Exit val: " + exitVal);
				// TODO keep track of time when executing returned commands
				// TODO scoreboard variables

				if (this.command.getSource().getEntity() == null) {
					// If source was command block, update state
					BlockPos blockPos = new BlockPos(this.command.getSource().getPosition());
					String cbCommand = "data modify block " + blockPos.getX() + " " + blockPos.getY() + " "
							+ blockPos.getZ() + " SuccessCount set value " + exitVal;
					LOGGER.info(cbCommand);
					this.server.getCommands().performCommand(this.command.getSource(), cbCommand);
				}
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

	private Map<String, String> getVars() {
		// Basic variable substitution here
		Map<String, String> vars = new HashMap<String, String>();

		try {
			String args = command.getArgument("params", String.class);
			vars.put("$args", args);
		} catch (IllegalArgumentException e) {
			LOGGER.info("Illegal arg: " + e);
			vars.put("$args", "");
		}

		Entity sourceEntity = this.command.getSource().getEntity();
		vars.put("$src", sourceEntity == null ? "" : this.entityToString(sourceEntity));

		List<String> targets = new ArrayList<String>();
		try {
			for (Entity e : EntityArgument.getEntities(this.command, "targets")) {
				targets.add(this.entityToString(e));
			}
		} catch (CommandSyntaxException e) {
			LOGGER.info("Syntax error: " + e);
		} catch (IllegalArgumentException e) {
			LOGGER.info("Illegal arg: " + e);
		}
		vars.put("$targets", String.join(";", targets));

		return vars;
	}
}
