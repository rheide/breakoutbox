package com.colorfulwolf.breakoutbox;

import net.minecraft.commands.CommandSourceStack;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("breakoutbox")
public class BreakoutBox {
	// Directly reference a log4j logger.
	public static final UUID ID = UUID.fromString("4b2fa329-a748-4fa5-8e39-c8dac1190003");
	private static final Logger LOGGER = LogManager.getLogger();
	private Map<String, BobCommandOptions> commands = new TreeMap<String, BobCommandOptions>();
	private BobOptions options = new BobOptions();

	private final class ListCommand implements Command<CommandSourceStack> {
		@Override
		public int run(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
			try {
				ServerPlayer player = command.getSource().getPlayerOrException();

				// player.sendMessage(new TextComponent("List of breakoutbox commands:"), uuid);
				StringBuilder cmds = new StringBuilder("List of breakoutbox commands:\n");
				// Check if command requires OP for command block and op-only commands here
				for (String key : BreakoutBox.this.commands.keySet()) {
					// cmds.append(" - ");
					// cmds.append(key);
					// cmds.append("\n");
					// player.sendMessage(new TextComponent(" - " + key), uuid);
					cmds.append("  " + key + "\n");
				}
				player.sendMessage(new TextComponent(cmds.toString()), BreakoutBox.ID);
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
				player.sendMessage(new TextComponent("RELOADED " + BreakoutBox.this.commands.size() + " commands"),
						BreakoutBox.ID);
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
	};

	public BreakoutBox() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

		// Register ourselves for server and other game events we are interested in
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
				this.generateDefaultConfig(f);
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"breakoutbox: could not load configuration from " + f.getAbsolutePath() + ": " + e, e);
		}
		// ConfigFileTypeHandler
		// some preinit code
		LOGGER.info("HELLO FROM PREINIT - " + f.getAbsolutePath());
		LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
	}

	private void generateDefaultConfig(File file) throws IOException {
		PrintWriter writer = new PrintWriter(file);
		writer.println("commandTimeoutSeconds=" + this.options.commandTimeoutSeconds);
		writer.println("commandThreads=" + this.options.commandThreads);

		BobCommandOptions bco = new BobCommandOptions();
		bco.path = "C:\\Windows\\system32\\calc.exe";
		this.commands.put("calc", bco);
		writer.println("[calc]");
		bco.write(writer);

		writer.flush();
		writer.close();
		LOGGER.info("No configuration file was found. A new file was written to " + file.getAbsolutePath());
	}

	private void loadConfig(File file) throws IOException {
		// Yes, this is nasty. Fuck standard java for not providing better standard libs
		// for json or yaml
		// I had to write this manually 10 years ago and I still have to write it
		// manually. This is why I switched to python..
		commands.clear();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String command = null;
		while ((line = br.readLine()) != null) {
			line = line.strip();
			if (line.length() == 0 || line.startsWith("//") || line.startsWith("#")) {
				continue;
			}
			if (line.contains("=")) {
				String[] split = line.split("=", 2);
				String key = split[0].strip();
				String value = split[1].strip();
				if (command == null || command.equalsIgnoreCase("common")) {
					this.parseCommonKey(key, value);
				} else {
					this.parseCommandKey(command, key, value);
				}
			} else if (line.startsWith("[")) {
				command = line.strip().replace("[", "").replace("]", "");
			}
		}
		br.close();
	}

	private void parseCommonKey(String key, String value) {
		if (key.equalsIgnoreCase("commandTimeoutSeconds")) {
			this.options.commandTimeoutSeconds = Long.parseLong(value);
		} else if (key.equalsIgnoreCase("commandThreads")) {
			this.options.commandThreads = Integer.parseInt(value);
		}
	}

	private void parseCommandKey(String command, String key, String value) {
		if (!commands.containsKey(command)) {
			commands.put(command, new BobCommandOptions());
		}
		commands.get(command).parse(key, value);
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
		// some example code to dispatch IMC to another mod
		InterModComms.sendTo("breakoutbox", "helloworld", () -> {
			LOGGER.info("Hello world from the MDK");
			return "Hello world";
		});
	}

	private void processIMC(final InterModProcessEvent event) {
		// some example code to receive and process InterModComms from other mods
		LOGGER.info("Got IMC {}",
				event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
		// do something when the server starts
		LOGGER.info("HELLO from server starting");
		final MinecraftServer server = event.getServer();
		CommandDispatcher<CommandSourceStack> disp = server.getCommands().getDispatcher();
		// event.getServer().getCommands().getDispatcher().register(Commands.literal("bob").executes(new
		// BobCommand(event.getServer())));
		BobCommand cmd = new BobCommand(event.getServer(), this.options, this.commands);

		// Optional messageargument at the end
		// bob list
		// bob reload
		// bob run cmd
		// bob run cmd @e
		// bob run cmd "message"
		// bob run cmd @e "message"

		/**
		 * Examples: - dynamic maps - easy clocks - IoT - animal pressure plate trigger
		 * light change rube goldberg machine: real life event trigger minecraft change
		 * triggers real life event
		 */
		disp.register(Commands.literal("bob").then(Commands.literal("list").executes(new ListCommand())));
		disp.register(Commands.literal("bob").then(Commands.literal("reload").executes(new ReloadCommand())));
		// disp.register(
		// Commands.literal("bob").then(Commands.argument("targets",
		// EntityArgument.entities()).executes(cmd)));

//        disp.register(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).executes(bleh).then(Commands.argument("count", IntegerArgumentType.integer(1)).executes((p_137775_) -> {
//            return giveItem(p_137775_.getSource(), ItemArgument.getItem(p_137775_, "item"), EntityArgument.getPlayers(p_137775_, "targets"), IntegerArgumentType.getInteger(p_137775_, "count"));
//         })))));
	}

	// You can use EventBusSubscriber to automatically subscribe events on the
	// contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			// register a new block here
			LOGGER.info("HELLO from Register Block");
		}
	}
}
