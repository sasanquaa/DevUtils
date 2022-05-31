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

	CommandContextParser(final Set<Argument<?>> argumentSet) {
		final StringBuilder stringBuilder = new StringBuilder();
		this.argumentSet = ImmutableList.copyOf(argumentSet);
		this.argumentSet.forEach(k -> stringBuilder.append(
				k.isFlag() ? flagUsage(k) : k.isOptional() ? optionalUsage(k) : requiredUsage(k)).append(" "));
		this.usage = stringBuilder.toString();
	}

	String getUsage() {
		return usage;
	}

	private void parseArgument(final Argument<?> argument, final ArgumentReader reader, final Consumer<Object> consumer)
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
		if (argument.isOptional()) {
			try {
				consumer.accept(argument.parseOrThrow(reader));
			} catch (final Exception ignored) {
			}
			return;
		}
		consumer.accept(argument.parseOrThrow(reader));
	}

	CommandContext parse(final String[] arguments) throws ArgumentParsingException {
		final ArgumentReader reader = new ArgumentReader(arguments);
		final Map<Argument<?>, Object> values = new HashMap<>();
		for (final Argument<?> argument : argumentSet) {
			parseArgument(argument, reader, (value) -> values.put(argument, value));
		}
		return new CommandContext(values);
	}

	List<String> getTabCompletions(final String[] arguments) {
		final ArgumentReader reader = new ArgumentReader(arguments);
		for (final Argument<?> key : argumentSet) {
			try {
				if (key.isOptional()) {
					key.parseOrThrow(reader);
				} else {
					parseArgument(key, reader, (ignored) -> {
					});
				}
			} catch (final ArgumentParsingException e) {
				return key.getTabCompletionsIfAny();
			}
		}
		return Collections.EMPTY_LIST;
	}

	private String requiredUsage(@UnknownInitialization CommandContextParser this, final Argument<?> key) {
		return "<" + key.getId() + ">";
	}

	private String flagUsage(@UnknownInitialization CommandContextParser this, final Argument<?> key) {
		return "[-" + key.getId() + " value]";
	}

	private String optionalUsage(@UnknownInitialization CommandContextParser this, final Argument<?> key) {
		return "[" + key.getId() + "]";
	}

}
