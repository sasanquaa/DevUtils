package me.sasanqua.utils.forge.def;

import net.minecraft.command.CommandException;

public class ArgumentParsingException extends CommandException {

	public ArgumentParsingException(final String message, final Object... objects) {
		super(message, objects);
	}

}
