package me.sasanqua.utils.forge;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class TextCallbackCommand extends CommandBase {

	private static final Map<UUID, TextCallback> CALLBACK_MAP = new HashMap<>();
	private static String callbackCommand = "tcallback";

	@Override
	public String getName() {
		return callbackCommand;
	}

	@Override
	public String getUsage(final ICommandSender sender) {
		return "/" + callbackCommand + " <uuid>";
	}

	@Override
	public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) {
		if (sender instanceof EntityPlayerMP) {
			final EntityPlayerMP player = (EntityPlayerMP) sender;
			if (args.length == 1) {
				try {
					final UUID uuid = UUID.fromString(args[0]);
					if (CALLBACK_MAP.containsKey(uuid)) {
						CALLBACK_MAP.get(uuid).tryInvokeConsumer(player);
						CALLBACK_MAP.values().removeIf(t -> System.currentTimeMillis() > t.persistTimestamp);
					}
				} catch (final IllegalArgumentException ignored) {
				}
			}
		}
	}

	@Override
	public boolean checkPermission(final MinecraftServer server, final ICommandSender sender) {
		return true;
	}

	static <T extends ITextComponent> T addCallback(final T text, final Consumer<EntityPlayerMP> consumer, final boolean invokeOnlyOnce) {
		final TextCallback callback = new TextCallback(consumer, invokeOnlyOnce);
		CALLBACK_MAP.put(callback.id, callback);
		final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/" + callbackCommand + " " + callback.id);
		text.getStyle().setClickEvent(clickEvent);
		return text;
	}

	static String getCallbackCommand() {
		return callbackCommand;
	}

	static void setCallbackCommand(final String command) {
		callbackCommand = command;
	}

	private static final class TextCallback {

		final UUID id;
		final Consumer<EntityPlayerMP> consumer;
		final Set<UUID> playersInvoked = new HashSet<>();
		final boolean onlyInvokeOnce;
		final long persistTimestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);

		TextCallback(final Consumer<EntityPlayerMP> consumer, final boolean onlyInvokeOnce) {
			this.id = UUID.randomUUID();
			this.consumer = consumer;
			this.onlyInvokeOnce = onlyInvokeOnce;
		}

		void tryInvokeConsumer(final EntityPlayerMP player) {
			if (onlyInvokeOnce && playersInvoked.contains(player.getUniqueID())) {
				return;
			}
			consumer.accept(player);
			playersInvoked.add(player.getUniqueID());
		}

	}
}
