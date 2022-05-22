package me.sasanqua.utils.forge;

import net.minecraft.command.CommandException;

@FunctionalInterface
public interface CommandExecutor {

	void execute(CommandContext context) throws CommandException;

}
