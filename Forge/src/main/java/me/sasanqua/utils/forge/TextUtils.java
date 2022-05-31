package me.sasanqua.utils.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TextUtils {

	public static final PlaceholderMatcher CURLY_PLACEHOLDER_MATCHER = placeholderMatcher(
			Pattern.compile("[{][^{}\\s]+[}]"), ",", "|");

	private static final Map<String, PlaceholderParser> PLACEHOLDER_PARSER_MAP = new HashMap<>();
	private static final Map<UUID, TextCallback> CALLBACK_MAP = new HashMap<>();
	private static String callbackCommand = "tcallback";

	public static String toLegacy(final String str) {
		return str.replace("&", "§");
	}

	public static String toModern(final String str) {
		return str.replace("§", "&");
	}

	public static StringTextComponent deserialize(final String str) {
		return new StringTextComponent(toLegacy(str));
	}

	public static String serialize(final StringTextComponent str) {
		return toModern(str.getText());
	}

	public static List<StringTextComponent> lore(final String... lore) {
		return Stream.of(lore).map(TextUtils::deserialize).collect(Collectors.toList());
	}

	public static List<StringTextComponent> lore(final List<String> lore) {
		return lore.stream().map(TextUtils::deserialize).collect(Collectors.toList());
	}

	public static <T extends ITextComponent> T callback(final T text, final Consumer<ServerPlayerEntity> consumer, final boolean invokeOnlyOnce) {
		final TextCallback callback = new TextCallback(consumer, invokeOnlyOnce);
		CALLBACK_MAP.put(callback.id, callback);
		final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/" + callbackCommand + " " + callback.id);
		text.getStyle().withClickEvent(clickEvent);
		return text;
	}

	public static void registerCallbackCommand(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal(callbackCommand)
				.then(Commands.argument("uuid", UUIDArgument.uuid()).executes(context -> {
					final UUID uuid = UUIDArgument.getUuid(context, "uuid");
					if (CALLBACK_MAP.containsKey(uuid)) {
						CALLBACK_MAP.get(uuid).tryInvokeConsumer(context.getSource().getPlayerOrException());
						CALLBACK_MAP.values().removeIf(t -> System.currentTimeMillis() > t.persistTimestamp);
					}
					return 0;
				})));
	}

	public static String getCallbackCommand() {
		return callbackCommand;
	}

	public static void setCallbackCommand(final String command) {
		callbackCommand = command;
	}

	public static void registerPlaceholder(final String key, final PlaceholderParser parser) {
		PLACEHOLDER_PARSER_MAP.put(key, parser);
	}

	public static TextPlaceholderParser parsePlaceholder(final String input) {
		return new TextPlaceholderParser(input);
	}

	public static PlaceholderMatcher placeholderMatcher(final Pattern pattern, final String argumentsSeparator, final String placeholderArgumentsSepartor) {
		return new PlaceholderMatcher(pattern, argumentsSeparator, placeholderArgumentsSepartor);
	}

	public static final class TextPlaceholderParser {

		private final Map<String, PlaceholderContext.PlaceholderContextBuilder> contextBuilderMap = new HashMap<>();
		private final String input;
		private PlaceholderMatcher placeholderMatcher = CURLY_PLACEHOLDER_MATCHER;

		private TextPlaceholderParser(final String input) {
			this.input = input;
		}

		public TextPlaceholderParser add(final String key, final Object... objects) {
			contextBuilderMap.putIfAbsent(key,
					contextBuilderMap.getOrDefault(key, new PlaceholderContext.PlaceholderContextBuilder())
							.objects(objects));
			return this;
		}

		public TextPlaceholderParser matcher(final PlaceholderMatcher matcher) {
			this.placeholderMatcher = matcher;
			return this;
		}

		@SuppressWarnings("RedundantCast")
		public StringTextComponent parse() {
			final StringBuilder output = new StringBuilder();
			final Matcher matcher = placeholderMatcher.getPattern().matcher(input);

			int i = 0;
			for (; matcher.find(); i = matcher.end()) {
				output.append(input, i, matcher.start());
				final String[] tokens = input.substring(matcher.start() + 1, matcher.end() - 1)
						.split(Pattern.quote(placeholderMatcher.getPlaceholderArgumentsSeparator()));
				final String placeholder = tokens[0];
				final String[] arguments = tokens.length > 1 ? String.join("",
								(@NonNull String[]) Arrays.copyOfRange(tokens, 1, tokens.length))
						.split(Pattern.quote(placeholderMatcher.getArgumentsSeparator())) : new String[0];
				final String result = Optional.ofNullable(contextBuilderMap.get(placeholder))
						.map(contextBuilder -> contextBuilder.arguments(arguments).build())
						.flatMap(context -> PLACEHOLDER_PARSER_MAP.getOrDefault(placeholder, (c) -> Optional.empty())
								.parse(context))
						.orElse(matcher.group());
				output.append(result);
			}

			if (i < input.length()) {
				output.append(input.substring(i));
			}
			return deserialize(output.toString());
		}

	}

	private static final class TextCallback {

		final UUID id;
		final Consumer<ServerPlayerEntity> consumer;
		final Set<UUID> playersInvoked = new HashSet<>();
		final boolean onlyInvokeOnce;
		final long persistTimestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);

		TextCallback(final Consumer<ServerPlayerEntity> consumer, final boolean onlyInvokeOnce) {
			this.id = UUID.randomUUID();
			this.consumer = consumer;
			this.onlyInvokeOnce = onlyInvokeOnce;
		}

		void tryInvokeConsumer(final ServerPlayerEntity player) {
			if (onlyInvokeOnce && playersInvoked.contains(player.getUUID())) {
				return;
			}
			consumer.accept(player);
			playersInvoked.add(player.getUUID());
		}

	}

}
