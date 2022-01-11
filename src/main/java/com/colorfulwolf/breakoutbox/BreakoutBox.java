package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("breakoutbox")
public class BreakoutBox {

	private Map<String, BobCommand> commands = new TreeMap<String, BobCommand>();
	private static final Logger LOGGER = LogManager.getLogger();

	private final class ListCommand implements Command<CommandSourceStack> {
		@Override
		public int run(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
			try {
				ServerPlayer player = command.getSource().getPlayerOrException();
				String[] ops = command.getSource().getServer().getPlayerList().getOpNames();
				boolean isOp = Arrays.asList(ops).contains(player.getName().getContents());
				StringBuilder cmds = new StringBuilder("List of breakoutbox commands:\n");
				for (String key : BreakoutBox.this.commands.keySet()) {
					BobCommand cmd = BreakoutBox.this.commands.get(key);
					if (cmd.runAsRegularPlayer || isOp) {
						cmds.append("  " + key + "\n");
					}
				}
				player.sendMessage(new TextComponent(cmds.toString()), Constants.ID);
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
	};

	private final class ReloadCommand implements Command<CommandSourceStack> {
		@Override
		public int run(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
			try {
				ServerPlayer player = command.getSource().getPlayerOrException();
				BreakoutBox.this.loadConfig(BreakoutBox.this.getConfigFile());
				player.sendMessage(new TextComponent("Reloaded " + BreakoutBox.this.commands.size() + " commands"),
						Constants.ID);
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
	};

	public BreakoutBox() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private File getConfigFile() {
		return new File("./breakoutbox.yaml");
	}

	private void setup(final FMLCommonSetupEvent event) {
		File f = this.getConfigFile();
		try {
			if (f.exists()) {
				this.loadConfig(f);
			} else {
				LOGGER.warn("No configuration file was found at " + f.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"breakoutbox: could not load configuration from " + f.getAbsolutePath() + ": " + e, e);
		}
	}

	private void loadConfig(File file) throws IOException {
		// I cannot believe standard java still does not have json or yaml parsers..
		commands.clear();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String command = null;
		while ((line = br.readLine()) != null) {
			line = line.strip();
			if (line.length() == 0 || line.startsWith("//") || line.startsWith("#")) {
				continue;
			}
			if (line.startsWith("[")) {
				command = line.strip().replace("[", "").replace("]", "");
				commands.put(command, new BobCommand());
			} else if (line.contains("=")) {
				String[] split = line.split("=", 2);
				String key = split[0].strip();
				String value = split[1].strip();
				commands.get(command).parse(key, value);
			}
		}
		br.close();
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
		final MinecraftServer server = event.getServer();
		CommandDispatcher<CommandSourceStack> disp = server.getCommands().getDispatcher();

		/**
		 * Examples: - dynamic maps - easy clocks - IoT - animal pressure plate trigger
		 * light change rube goldberg machine: real life event trigger minecraft change
		 * triggers real life event echo with variables
		 */
		BobCommandParser cmd = new BobCommandParser(server, this.commands);
		LiteralArgumentBuilder<CommandSourceStack> bob = Commands.literal("bob");
		disp.register(bob.then(Commands.literal("list").executes(new ListCommand())));

		disp.register(bob.then(Commands.literal("reload").requires((pred) -> {
			return pred.hasPermission(Constants.PERM_OP);
		}).executes(new ReloadCommand())));

		disp.register(bob.then(Commands.literal("run").then(Commands.argument("cmd", StringArgumentType.word())
				.executes(cmd).then(Commands.argument("params", StringArgumentType.greedyString()).executes(cmd)))));

		disp.register(
				bob.then(Commands.literal("runtarget")
						.then(Commands.argument("cmd", StringArgumentType.word()).then(Commands
								.argument("targets", EntityArgument.entities()).executes(cmd)
								.then(Commands.argument("params", StringArgumentType.greedyString()).executes(cmd))))));
	}
}
