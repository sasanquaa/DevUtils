package me.sasanqua.utils.forge;

import net.minecraft.command.CommandException;

import java.util.Map;
import java.util.Optional;

public final class ArgumentContext {

	private final Map<ArgumentKey<?>, Object> values;

	ArgumentContext(Map<ArgumentKey<?>, Object> values) {
		this.values = values;
	}

	public <T> Optional<T> find(ArgumentKey<T> key) {
		return Optional.ofNullable((T) values.get(key));
	}

	public <T> T get(ArgumentKey<T> key) throws CommandException {
		return find(key).orElseThrow(
				() -> new CommandException("Argument key with id " + key.getId() + " not found inside context!"));
	}

}
