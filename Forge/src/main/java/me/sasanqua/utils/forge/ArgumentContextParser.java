package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.def.ArgumentParsingException;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public final class ArgumentContextParser {

	private static final String FLAG_PREFIX = "-";

	private final Set<ArgumentKey<?>> keySet;
	private final String usage;

	private ArgumentContextParser(Builder builder) {
		StringBuilder stringBuilder = new StringBuilder();
		this.keySet = Collections.unmodifiableSet(builder.keySet);
		this.keySet.forEach(k -> stringBuilder.append(k.isFlag() ? optionalUsage(k) : requiredUsage(k)).append(" "));
		this.usage = stringBuilder.toString();
	}

	public String getUsage() {
		return usage;
	}

	public ArgumentContext parse(String[] arguments) throws ArgumentParsingException {
		return parse(arguments, null);
	}

	public ArgumentContext parse(String[] arguments, @Nullable String errorMessage) throws ArgumentParsingException {
		ArgumentReader reader = new ArgumentReader(arguments);
		Map<ArgumentKey<?>, Object> values = new HashMap<>();
		for (ArgumentKey<?> key : keySet) {
			if (key.isFlag()) {
				if (reader.peek().startsWith(FLAG_PREFIX)) {
					if (!reader.canAdvance()) {
						throw new ArgumentParsingException("Flag " + reader.peek() + " provided but cannot continue!");
					}
					if (reader.peek().startsWith(FLAG_PREFIX + key.getId())) {
						reader.advance();
						values.put(key, parseOrThrow(key.getParser(), reader, errorMessage));
					}
				}
				continue;
			}
			values.put(key, parseOrThrow(key.getParser(), reader, errorMessage));
		}
		return new ArgumentContext(values);
	}

	private Object parseOrThrow(ArgumentParser<?> parser, ArgumentReader reader, @Nullable String errorMessage)
			throws ArgumentParsingException {
		try {
			return PreconditionUtils.checkNotNull(parser.parse(reader));
		} catch (Exception e) {
			throw new ArgumentParsingException(errorMessage == null ? String.valueOf(e.getMessage()) : errorMessage, e);
		}
	}

	private String requiredUsage(@UnknownInitialization ArgumentContextParser this, ArgumentKey<?> key) {
		return "<" + key.getId() + ">";
	}

	private String optionalUsage(@UnknownInitialization ArgumentContextParser this, ArgumentKey<?> key) {
		return "[-" + key.getId() + " value]";
	}

	public static final class Builder {

		private final Set<ArgumentKey<?>> keySet = new LinkedHashSet<>();

		Builder() {
		}

		public Builder add(ArgumentKey<?> key) {
			keySet.add(key);
			return this;
		}

		public ArgumentContextParser build() {
			return new ArgumentContextParser(this);
		}

	}

}
