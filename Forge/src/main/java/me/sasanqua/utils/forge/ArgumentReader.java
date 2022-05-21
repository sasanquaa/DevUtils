package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.PreconditionUtils;

public final class ArgumentReader {

	private final String[] arguments;
	private int current = 0;

	ArgumentReader(String[] arguments) {
		this.arguments = arguments;
	}

	public boolean canAdvance() {
		return current < arguments.length;
	}

	public String peek() {
		return arguments[Math.min(current, arguments.length - 1)];
	}

	public String advance() throws IllegalStateException {
		PreconditionUtils.checkState(canAdvance(), "Unable to advance to next argument");
		return arguments[current++];
	}

}
