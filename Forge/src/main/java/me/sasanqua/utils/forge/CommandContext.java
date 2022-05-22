package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.PreconditionUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Map;
import java.util.Optional;

public final class CommandContext {

	private final Map<Argument<?>, Object> values;
	private @MonotonicNonNull MinecraftServer server;
	private @MonotonicNonNull ICommandSender sender;

	CommandContext(Map<Argument<?>, Object> values) {
		this.values = values;
	}

	public MinecraftServer getServer() {
		return PreconditionUtils.checkNotNull(server);
	}

	public ICommandSender getSender() {
		return PreconditionUtils.checkNotNull(sender);
	}

	void setServer(MinecraftServer server) {
		this.server = server;
	}

	void setSender(ICommandSender sender) {
		this.sender = sender;
	}

	public <T> Optional<T> find(Argument<T> key) {
		return Optional.ofNullable((T) values.get(key));
	}

	public <T> T get(Argument<T> key) throws CommandException {
		return find(key).orElseThrow(
				() -> new CommandException("Argument key with id " + key.getId() + " not found inside context!"));
	}

}
