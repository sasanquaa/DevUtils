package me.sasanqua.utils.forge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import me.sasanqua.utils.common.PreconditionUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TextUtils {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(^[^{]+)?([{][^{} ]+[}])(.+)?");
	private static final Map<String, PlaceholderParser> PLACEHOLDER_PARSER_MAP = new HashMap<>();
	private static final Map<UUID, TextCallback> CALLBACK_MAP = new HashMap<>();
	private static String callbackCommand = "tucallback";

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
		TextCallback callback = new TextCallback(consumer, invokeOnlyOnce);
		CALLBACK_MAP.put(callback.getUUID(), callback);
		ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/" + callbackCommand + " " + callback.getUUID().toString());
		text.getStyle().setClickEvent(clickEvent);
		return text;
	}

	public static void registerPlaceholder(String key, PlaceholderParser parser) {
		PLACEHOLDER_PARSER_MAP.put(key, parser);
	}

	public static TextPlaceholderParser parsePlaceholder(String input) {
		return new TextPlaceholderParser(input);
	}

	public String getCallbackCommand() {
		return callbackCommand;
	}

	public void setCallbackCommand(String command) {
		callbackCommand = command;
	}

	@FunctionalInterface
	public interface PlaceholderParser {
		Optional<String> parse(PlaceholderContext context);

	}

	public static class TextPlaceholderParser {

		private final Map<String, PlaceholderContext.PlaceholderContextBuilder> contextBuilderMap = new HashMap<>();
		private final String input;

		private TextPlaceholderParser(String input) {
			this.input = input;
		}

		public TextPlaceholderParser add(String key, Object... objects) {
			contextBuilderMap.putIfAbsent(key,
					contextBuilderMap.getOrDefault(key, new PlaceholderContext.PlaceholderContextBuilder())
							.objects(objects));
			return this;
		}

		public TextComponentString parse() {
			StringBuilder output = new StringBuilder();
			String working = input;

			while (!working.isEmpty()) {
				Matcher matcher = PLACEHOLDER_PATTERN.matcher(working);
				if (matcher.find()) {
					String[] token = PreconditionUtils.checkNotNull(matcher.group(2))
							.replace("{", "")
							.replace("}", "")
							.toLowerCase()
							.split("\\|");

					String placeholder = token[0];
					String[] arguments = token.length > 1 ? token[1].split(",") : new String[0];

					if (matcher.group(1) != null) {
						output.append(matcher.group(1));
						working = working.replaceFirst("^[^{]+", "");
					}
					Optional<String> result = Optional.ofNullable(contextBuilderMap.get(placeholder))
							.map(contextBuilder -> contextBuilder.arguments(arguments).build())
							.flatMap(
									context -> PLACEHOLDER_PARSER_MAP.getOrDefault(placeholder, (c) -> Optional.empty())
											.parse(context));
					if (result.isPresent()) {
						output.append(result.get());
					} else {
						output.append(matcher.group(2));
					}

					working = working.replaceFirst("[{][^{} ]+[}]", "");
				} else {
					output.append(working);
					break;
				}
			}
			return deserialize(output.toString());
		}

	}

	public static class PlaceholderContext {

		private final List<String> arguments;
		private final ListMultimap<Class<?>, Object> contextObjects;

		private PlaceholderContext(PlaceholderContextBuilder builder) {
			this.arguments = Collections.unmodifiableList(builder.arguments);
			this.contextObjects = Multimaps.unmodifiableListMultimap(builder.contextObjects);
		}

		public List<String> getArguments() {
			return arguments;
		}

		public <T> Optional<T> getAssociation(Class<T> clazz) {
			return contextObjects.get(clazz).stream().findFirst().map(clazz::cast);
		}

		public <T> List<T> getAllAssociations(Class<T> clazz) {
			return contextObjects.get(clazz)
					.stream()
					.filter(Objects::nonNull)
					.map(clazz::cast)
					.collect(Collectors.toList());
		}

		private static class PlaceholderContextBuilder {

			private List<String> arguments = new ArrayList<>();
			private ListMultimap<Class<?>, Object> contextObjects = ArrayListMultimap.create();

			private PlaceholderContextBuilder arguments(String... arguments) {
				this.arguments = Lists.newArrayList(arguments);
				return this;
			}

			private PlaceholderContextBuilder objects(Object... objects) {
				for (Object object : objects) {
					contextObjects.put(object.getClass(), object);
				}
				return this;
			}

			private PlaceholderContext build() {
				return new PlaceholderContext(this);
			}

		}

	}

	private static class TextCallbackCommand extends CommandBase {

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

	}

	private static class TextCallback {

		private UUID callbackUUID;
		private Consumer<EntityPlayerMP> consumer;
		private boolean onlyInvokeOnce;

		private Set<UUID> playersInvoked = new HashSet<>();

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
