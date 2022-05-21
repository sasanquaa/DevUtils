package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.exception.ArgumentParsingException;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;

import java.util.*;

public final class ArgumentContextParser {

	private static final String FLAG_PREFIX = "-";

	private final Set<ArgumentKey<?>> keySet;
	private final long nonFlagKeys;
	private final String usage;

	private ArgumentContextParser(Builder builder) {
		StringBuilder stringBuilder = new StringBuilder();
		this.keySet = Collections.unmodifiableSet(builder.keySet);
		this.nonFlagKeys = this.keySet.stream().filter(k -> {
			stringBuilder.append(k.isFlag() ? optionalUsage(k) : requiredUsage(k)).append(" ");
			return !k.isFlag();
		}).count();
		this.usage = stringBuilder.toString();
	}

	public String getUsage() {
		return usage;
	}

	public ArgumentContext parse(String[] arguments) throws ArgumentParsingException {
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
						values.put(key, parseOrThrow(key.getParser(), reader));
					}
				}
				continue;
			}
			values.put(key, parseOrThrow(key.getParser(), reader));
		}
		return new ArgumentContext(values);
	}

	private Object parseOrThrow(ArgumentParser<?> parser, ArgumentReader reader) throws ArgumentParsingException {
		try {
			return PreconditionUtils.checkNotNull(parser.parse(reader));
		} catch (Exception e) {
			throw new ArgumentParsingException(String.valueOf(e.getMessage()), e);
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
