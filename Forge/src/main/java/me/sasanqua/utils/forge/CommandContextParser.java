package me.sasanqua.utils.forge;

import com.google.common.collect.ImmutableList;
import me.sasanqua.utils.forge.def.ArgumentParsingException;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;

import java.util.*;
import java.util.function.Consumer;

final class CommandContextParser {

	private static final String FLAG_PREFIX = "-";

	private final List<Argument<?>> argumentSet;
	private final String usage;

	CommandContextParser(Set<Argument<?>> argumentSet) {
		StringBuilder stringBuilder = new StringBuilder();
		this.argumentSet = ImmutableList.copyOf(argumentSet);
		this.argumentSet.forEach(
				k -> stringBuilder.append(k.isFlag() ? optionalUsage(k) : requiredUsage(k)).append(" "));
		this.usage = stringBuilder.toString();
	}

	String getUsage() {
		return usage;
	}

	private void parseArgument(Argument<?> argument, ArgumentReader reader, Consumer<Object> consumer)
			throws ArgumentParsingException {
		if (argument.isFlag()) {
			if (reader.peek().startsWith(FLAG_PREFIX)) {
				if (!reader.canAdvance()) {
					throw new ArgumentParsingException("Flag " + reader.peek() + " provided but cannot continue!");
				}
				if (reader.peek().startsWith(FLAG_PREFIX + argument.getId())) {
					reader.advance();
					consumer.accept(argument.parseOrThrow(reader));
				}
			}
			return;
		}
		consumer.accept(argument.parseOrThrow(reader));
	}

	CommandContext parse(String[] arguments) throws ArgumentParsingException {
		ArgumentReader reader = new ArgumentReader(arguments);
		Map<Argument<?>, Object> values = new HashMap<>();
		for (Argument<?> argument : argumentSet) {
			parseArgument(argument, reader, (value) -> values.put(argument, value));
		}
		return new CommandContext(values);
	}

	List<String> getTabCompletions(String[] arguments) {
		ArgumentReader reader = new ArgumentReader(arguments);
		for (Argument<?> key : argumentSet) {
			try {
				parseArgument(key, reader, (ignored) -> {
				});
			} catch (ArgumentParsingException e) {
				return key.getTabCompletionsIfAny();
			}
		}
		return Collections.EMPTY_LIST;
	}

	private String requiredUsage(@UnknownInitialization CommandContextParser this, Argument<?> key) {
		return "<" + key.getId() + ">";
	}

	private String optionalUsage(@UnknownInitialization CommandContextParser this, Argument<?> key) {
		return "[-" + key.getId() + " value]";
	}

}
