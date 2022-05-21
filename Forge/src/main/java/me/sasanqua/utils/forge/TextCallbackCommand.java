package me.sasanqua.utils.forge;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

import java.util.*;
import java.util.function.Consumer;

final class TextCallbackCommand extends CommandBase {

	private static final Map<UUID, TextCallback> CALLBACK_MAP = new HashMap<>();
	private static String callbackCommand = "tcallback";

	static <T extends ITextComponent> T addCallback(T text, Consumer<EntityPlayerMP> consumer, boolean invokeOnlyOnce) {
		TextCallback callback = new TextCallback(consumer, invokeOnlyOnce);
		CALLBACK_MAP.put(callback.getUUID(), callback);
		ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/" + callbackCommand + " " + callback.getUUID().toString());
		text.getStyle().setClickEvent(clickEvent);
		return text;
	}

	static String getCallbackCommand() {
		return callbackCommand;
	}

	static void setCallbackCommand(String command) {
		callbackCommand = command;
	}

	@Override
	public String getName() {
		return callbackCommand;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/" + callbackCommand + " <uuid>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) sender;
			if (args.length == 1) {
				try {
					UUID uuid = UUID.fromString(args[0]);
					if (CALLBACK_MAP.containsKey(uuid)) {
						CALLBACK_MAP.get(uuid).tryInvokeConsumer(player);
					}
				} catch (IllegalArgumentException ignored) {
				}
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	private static final class TextCallback {

		UUID callbackUUID;
		Consumer<EntityPlayerMP> consumer;
		boolean onlyInvokeOnce;
		Set<UUID> playersInvoked = new HashSet<>();

		TextCallback(Consumer<EntityPlayerMP> consumer, boolean onlyInvokeOnce) {
			this.callbackUUID = UUID.randomUUID();
			this.consumer = consumer;
			this.onlyInvokeOnce = onlyInvokeOnce;
		}

		UUID getUUID() {
			return callbackUUID;
		}

		void tryInvokeConsumer(EntityPlayerMP player) {
			if (onlyInvokeOnce && playersInvoked.contains(player.getUniqueID())) {
				return;
			}
			consumer.accept(player);
			playersInvoked.add(player.getUniqueID());
		}

	}
}
