package me.sasanqua.utils.forge.exception;

import net.minecraft.command.CommandException;

public class ArgumentParsingException extends CommandException {

	public ArgumentParsingException(String message, Object... objects) {
		super(message, objects);
	}

}
