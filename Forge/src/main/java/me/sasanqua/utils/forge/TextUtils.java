package me.sasanqua.utils.forge;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TextUtils {

	public static final PlaceholderMatcher CURLY_PLACEHOLDER_MATCHER = placeholderMatcher(
			Pattern.compile("[{][^{}\\s]+[}]"), ",", "|");

	private static final Map<String, PlaceholderParser> PLACEHOLDER_PARSER_MAP = new HashMap<>();

	public static String toLegacy(String str) {
		return str.replace("&", "§");
	}

	public static String toModern(String str) {
		return str.replace("§", "&");
	}

	public static TextComponentString deserialize(String str) {
		return new TextComponentString(toLegacy(str));
	}

	public static String serialize(TextComponentString str) {
		return toModern(str.getFormattedText());
	}

	public static List<TextComponentString> lore(String... lore) {
		return Stream.of(lore).map(TextUtils::deserialize).collect(Collectors.toList());
	}

	public static List<TextComponentString> lore(List<String> lore) {
		return lore.stream().map(TextUtils::deserialize).collect(Collectors.toList());
	}

	public static void registerCallbackCommand(Consumer<ICommand> consumer) {
		consumer.accept(new TextCallbackCommand());
	}

	public static <T extends ITextComponent> T addCallback(T text, Consumer<EntityPlayerMP> consumer, boolean invokeOnlyOnce) {
		return TextCallbackCommand.addCallback(text, consumer, invokeOnlyOnce);
	}

	public static String getCallbackCommand() {
		return TextCallbackCommand.getCallbackCommand();
	}

	public static void setCallbackCommand(String command) {
		TextCallbackCommand.setCallbackCommand(command);
	}

	public static void registerPlaceholder(String key, PlaceholderParser parser) {
		PLACEHOLDER_PARSER_MAP.put(key, parser);
	}

	public static TextPlaceholderParser parsePlaceholder(String input) {
		return new TextPlaceholderParser(input);
	}

	public static PlaceholderMatcher placeholderMatcher(Pattern pattern, String argumentsSeparator, String placeholderArgumentsSepartor) {
		return new PlaceholderMatcher(pattern, argumentsSeparator, placeholderArgumentsSepartor);
	}

	public static final class TextPlaceholderParser {

		private final Map<String, PlaceholderContext.PlaceholderContextBuilder> contextBuilderMap = new HashMap<>();
		private final String input;
		private PlaceholderMatcher placeholderMatcher = CURLY_PLACEHOLDER_MATCHER;

		private TextPlaceholderParser(String input) {
			this.input = input;
		}

		public TextPlaceholderParser add(String key, Object... objects) {
			contextBuilderMap.putIfAbsent(key,
					contextBuilderMap.getOrDefault(key, new PlaceholderContext.PlaceholderContextBuilder())
							.objects(objects));
			return this;
		}

		public TextPlaceholderParser matcher(PlaceholderMatcher matcher) {
			this.placeholderMatcher = matcher;
			return this;
		}

		public TextComponentString parse() {
			StringBuilder output = new StringBuilder();
			Matcher matcher = placeholderMatcher.getPattern().matcher(input);

			int i = 0;
			for (; matcher.find(); i = matcher.end()) {
				output.append(input, i, matcher.start());
				String[] tokens = input.substring(matcher.start() + 1, matcher.end() - 1)
						.split(Pattern.quote(placeholderMatcher.getPlaceholderArgumentsSeparator()));
				String placeholder = tokens[0];
				String[] arguments = tokens.length > 1 ? String.join("",
								(@NonNull String[]) Arrays.copyOfRange(tokens, 1, tokens.length))
						.split(Pattern.quote(placeholderMatcher.getArgumentsSeparator())) : new String[0];
				String result = Optional.ofNullable(contextBuilderMap.get(placeholder))
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

}
