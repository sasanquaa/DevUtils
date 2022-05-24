package me.sasanqua.utils.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import me.sasanqua.utils.common.PreconditionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import scala.actors.threadpool.Arrays;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class CommandSpec {

	private final String permission;
	private final @Nullable String usage;
	private final Multimap<CommandSpec, String> children;
	private final CommandExecutor executor;
	private final CommandContextParser parser;

	private CommandSpec(Builder builder) {
		this.permission = PreconditionUtils.checkNotNull(builder.permission, "Permission must not be null");
		this.usage = builder.usage;
		this.children = Multimaps.unmodifiableMultimap(builder.children);
		this.executor = PreconditionUtils.checkNotNull(builder.executor, "Executor must not be null");
		this.parser = new CommandContextParser(builder.argumentSet);
	}

	Optional<String> getUsage() {
		return Optional.ofNullable(usage);
	}

	String getPermission() {
		return permission;
	}

	Multimap<CommandSpec, String> getChildren() {
		return children;
	}

	CommandExecutor getExecutor() {
		return executor;
	}

	CommandContextParser getParser() {
		return parser;
	}

	public static final class Builder implements me.sasanqua.utils.common.Builder<CommandSpec> {

		private @Nullable String usage;
		private @Nullable String permission;
		private final Multimap<CommandSpec, String> children = HashMultimap.create();
		private final Set<Argument<?>> argumentSet = new LinkedHashSet<>();

		private @Nullable CommandExecutor executor;

		Builder() {
		}

		public Builder addArgument(Argument<?> key) {
			PreconditionUtils.checkState(children.isEmpty(), "Cannot add arguments along with children specs");
			argumentSet.add(key);
			return this;
		}

		public Builder addChild(CommandSpec commandSpec, String... keys) {
			PreconditionUtils.checkArgument(!children.containsKey(commandSpec), "Command spec already existed");
			PreconditionUtils.checkArgument(keys.length > 0, "Keys must not be empty");
			PreconditionUtils.checkState(argumentSet.isEmpty(), "Cannot add child along with self arguments");
			children.putAll(commandSpec, Arrays.asList(keys));
			return this;
		}

		public Builder permission(String permission) {
			this.permission = permission;
			return this;
		}

		public Builder usage(String usage) {
			this.usage = usage;
			return this;
		}

		public Builder executor(CommandExecutor executor) {
			this.executor = executor;
			return this;
		}

		@Override
		public CommandSpec build() {
			return new CommandSpec(this);
		}

	}

}
