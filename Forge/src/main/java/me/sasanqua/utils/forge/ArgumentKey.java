package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.Identifiable;
import me.sasanqua.utils.common.PreconditionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public final class ArgumentKey<T> implements Identifiable<String> {

	private final String id;
	private final ArgumentParser<T> parser;
	private final boolean flag;

	private ArgumentKey(Builder builder) {
		this.id = PreconditionUtils.checkNotNull(builder.id, "Key id must not be null");
		this.parser = PreconditionUtils.checkNotNull((ArgumentParser<T>) builder.parser, "Parser must not be null");
		this.flag = builder.flag;
	}

	@Override
	public String getId() {
		return id;
	}

	ArgumentParser<T> getParser() {
		return parser;
	}

	boolean isFlag() {
		return flag;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		return o instanceof ArgumentKey && Objects.equals(id, ((ArgumentKey<?>) o).id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public static final class Builder {

		private @Nullable String id;
		private @Nullable ArgumentParser<?> parser;
		private boolean flag = false;

		Builder() {
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder parser(ArgumentParser<?> parser) {
			this.parser = parser;
			return this;
		}

		public Builder flag() {
			this.flag = true;
			return this;
		}

		public <T> ArgumentKey<T> build() {
			return new ArgumentKey<>(this);
		}

	}

}
