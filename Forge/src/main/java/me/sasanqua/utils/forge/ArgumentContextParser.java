package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.PreconditionUtils;
import net.minecraft.command.CommandException;
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

	public ArgumentContext parse(String[] arguments) throws CommandException {
		ArgumentReader reader = new ArgumentReader(arguments);
		Map<ArgumentKey<?>, Object> values = new HashMap<>();
		for (ArgumentKey<?> key : keySet) {
			if (key.isFlag()) {
				if (reader.peek().startsWith(FLAG_PREFIX)) {
					if (!reader.canAdvance()) {
						throw new CommandException("Flag " + reader.peek() + " provided but cannot continue!");
					}
					if (reader.peek().startsWith(FLAG_PREFIX + key.getId())) {
						reader.advance();
						tryParse(key.getParser(), reader).ifPresent(value -> values.put(key, value));
					}
				}
				continue;
			}
			tryParse(key.getParser(), reader).ifPresent(value -> values.put(key, value));
		}
		if (values.keySet().stream().filter(k -> !k.isFlag()).count() != nonFlagKeys) {
			throw new CommandException("Invalid arguments provided");
		}
		return new ArgumentContext(values);
	}

	private Optional<Object> tryParse(ArgumentParser<?> parser, ArgumentReader reader) {
		try {
			return Optional.of(PreconditionUtils.checkNotNull(parser.parse(reader)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
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
