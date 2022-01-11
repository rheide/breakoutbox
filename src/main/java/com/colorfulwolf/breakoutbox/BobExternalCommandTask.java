package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
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
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;

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

	private String entityToString(Entity e, Objective objective) {
		String entityScore = "";
		if (objective != null) {
			Collection<Score> playerScores = objective.getScoreboard().getPlayerScores(objective);
			for (Score score : playerScores) {
				if (score.getOwner().equals(e.getScoreboardName())) {
					entityScore = "," + score.getScore();
					break;
				}
			}
		}
		return e.getName().getContents() + "," + e.getBlockX() + "," + e.getBlockY() + "," + e.getBlockZ() + entityScore;
	}

	@Override
	public void run() {
		LOGGER.debug("Executing " + this.cmd.path);

		Process process = null;
		try {
			Map<String, String> vars = getVars();

			// Why is escaping a string so hard in java???
			List<String> cmdArgs = new ArrayList<String>();
			Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(this.cmd.path);
			while (m.find()) {
				String cmdArg = m.group(1).replace("\"", "");
				for (String var : vars.keySet()) {
					cmdArg = cmdArg.replace(var, vars.get(var));
				}
				cmdArgs.add(cmdArg);
			}

			ProcessBuilder builder = new ProcessBuilder(cmdArgs);

			process = builder.start();
			if (!process.waitFor(this.cmd.commandTimeoutMilliseconds, TimeUnit.MILLISECONDS)) {
				process.destroy();
			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (this.cmd.parseOutput) {
						this.server.getCommands().performCommand(this.command.getSource(), line);						
					}
				}

				int exitVal = Math.max(Math.min(process.exitValue(), 15), 0);

				if (this.command.getSource().getEntity() == null) {
					// If source was command block, update state
					BlockPos pos = new BlockPos(this.command.getSource().getPosition());
					this.cmd.setLastResult(pos, exitVal);
					String cbCommand = String.format(Constants.SETCMD, pos.getX(), pos.getY(), pos.getZ(), exitVal);
					LOGGER.debug(cbCommand);
					this.server.getCommands().performCommand(this.command.getSource(), cbCommand);
				}
			}
		} catch (InterruptedException e) {
			LOGGER.debug("INTERRUPTED. KILLING");
			if (process != null) {
				process.destroy();
			}
		} catch (Exception e) {
			LOGGER.debug("EXC: " + e);
			if (process != null) {
				process.destroy();
			}
		}
	}

	private Map<String, String> getVars() {
		// Very basic variable substitution here
		Map<String, String> vars = new HashMap<String, String>();

		try {
			String args = command.getArgument("params", String.class);
			vars.put("$args", args);
		} catch (IllegalArgumentException e) {
			vars.put("$args", "");
		}

		vars.put("$objective", "");
		Objective objective = null;
		try {
			objective = ObjectiveArgument.getObjective(this.command, "objective");
			vars.put("$objective", objective.getName());
		}
		catch (CommandSyntaxException e) { e.printStackTrace(); }
		catch (IllegalArgumentException e) {}
		
		Entity sourceEntity = this.command.getSource().getEntity();
		vars.put("$src", sourceEntity == null ? "" : this.entityToString(sourceEntity, objective));

		List<String> targets = new ArrayList<String>();
		try {
			for (Entity e : EntityArgument.getEntities(this.command, "targets")) {
				targets.add(this.entityToString(e, objective));
			}
		} catch (CommandSyntaxException e) {
			LOGGER.info("Syntax error: " + e);
		} catch (IllegalArgumentException e) {}
		vars.put("$targets", String.join(";", targets));

		return vars;
	}
}
