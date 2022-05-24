package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.Identifiable;
import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.def.ArgumentParsingException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

public final class Argument<T> implements Identifiable<String> {

	private final String id;
	private final ArgumentParser<T> parser;
	private final boolean optional;
	private final boolean flag;
	private final String parsingErrorMessage;

	private Argument(Builder<T> builder) {
		this.id = PreconditionUtils.checkNotNull(builder.id, "Key id must not be null");
		this.parser = PreconditionUtils.checkNotNull(builder.parser, "Parser must not be null");
		this.optional = builder.optional;
		this.flag = builder.flag;
		this.parsingErrorMessage = builder.parsingErrorMessage == null ? "Invalid argument provided" : builder.parsingErrorMessage;
	}

	@Override
	public String getId() {
		return id;
	}

	Object parseOrThrow(ArgumentReader reader) throws ArgumentParsingException {
		try {
			return PreconditionUtils.checkNotNull(parser.parse(reader));
		} catch (Exception e) {
			throw new ArgumentParsingException(parsingErrorMessage, e);
		}
	}

	boolean isFlag() {
		return flag;
	}

	boolean isOptional() {
		return optional;
	}

	List<String> getTabCompletionsIfAny() {
		return TabCompleter.getTabCompletions(parser);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		return o instanceof Argument && Objects.equals(id, ((Argument<?>) o).id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public static final class Builder<T> implements me.sasanqua.utils.common.Builder<Argument<T>> {

		private @Nullable String id;
		private @Nullable ArgumentParser<T> parser;

		private @Nullable String parsingErrorMessage;
		private boolean optional = false;
		private boolean flag = false;

		Builder() {
		}

		public Builder<T> id(String id) {
			this.id = id;
			return this;
		}

		public Builder<T> parser(ArgumentParser<T> parser) {
			this.parser = parser;
			return this;
		}

		public Builder<T> parsingErrorMessage(String message) {
			this.parsingErrorMessage = message;
			return this;
		}

		public Builder<T> flag() {
			PreconditionUtils.checkState(!optional, "Optional cannot be used along with flag");
			this.flag = true;
			return this;
		}

		public Builder<T> optional() {
			PreconditionUtils.checkState(!flag, "Flag cannot be used along with optional");
			this.optional = true;
			return this;
		}

		@Override
		public Argument<T> build() {
			return new Argument<>(this);
		}

	}

}
