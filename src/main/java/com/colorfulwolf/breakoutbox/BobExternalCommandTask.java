package com.colorfulwolf.breakoutbox;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;

public class BobExternalCommandTask implements Runnable {

	private CommandContext<CommandSourceStack> command;

	public BobExternalCommandTask(CommandContext<CommandSourceStack> command) {
		 this.command = command;
	}
	
	@Override
	public void run() {
		 
		
	}
}
