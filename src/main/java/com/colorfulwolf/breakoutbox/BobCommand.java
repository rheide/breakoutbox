package com.colorfulwolf.breakoutbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
	private final ThreadPoolExecutor pool;
	
	private static final Logger LOGGER = LogManager.getLogger();

	public BobCommand(MinecraftServer server) {
		this.server = server;
		this.pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(8);
	}

	@Override
	public int run(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		try {
			Collection<? extends Entity> entities = EntityArgument.getEntities(command, "targets");
			LOGGER.info("entities: " + entities);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		LOGGER.info("/bob command dispatched 123");
        
        LOGGER.info("Input: " + command.getInput()); // full command text
        LOGGER.info("Source pos: " + command.getSource().getPosition()); // Position of command block or player typing the cmd
        
        // null (because command block?)
        // or Entity: ServerPlayer['RandyRanger'/169, l='ServerLevel[world]', x=355.43, y=71.00, z=-91.34]
        LOGGER.info("Entity: " + command.getSource().getEntity());
        
        LOGGER.info("Root: " + command.getRootNode().getName());
        
        LOGGER.info("LAST CHILD ENTITY: " + command.getLastChild().getSource().getEntity());
        //LOGGER.info("ROOT ENTITY: " + command.getRootNode().);
        
        List<ParsedCommandNode<CommandSourceStack>> nodes = command.getNodes();
        for (ParsedCommandNode<CommandSourceStack> node: nodes) {
        	LOGGER.info("Node: " + node);
        }
        
        
        try {
        	LOGGER.info("PLAYER: " + command.getSource().getPlayerOrException());            	
        }
        catch (Exception e) {
        	e.printStackTrace();
        }

        ProcessBuilder builder = new ProcessBuilder("C:\\workspace\\venv\\Scripts\\python.exe", "dostuff.py");
	    Process process;
		try {
			process = builder.start();
			StringBuilder out = new StringBuilder();
    	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
    	        String line = null;
    	      while ((line = reader.readLine()) != null) {
    	        out.append(line);
    	        out.append("\n");
    	      }
    	      LOGGER.info(out);
    	      this.server.getCommands().getDispatcher().execute("say HELLO", command.getSource());
    	      this.server.getCommands().getDispatcher().execute("data modify block 361 71 -92 SuccessCount set value 6", command.getSource());
    	    }
    	    //((BlockCommandSender) sender).getBlock().getState().setMetadata("SuccessCount", new MetadataValueOutput(state));
    	    //((BlockCommandSender) sender).getBlock().getState().update();
    	    int exitVal = process.exitValue();
    	    LOGGER.info("Exit val: " + exitVal);
    	    return process.exitValue();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	
}
