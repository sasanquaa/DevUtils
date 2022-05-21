package me.sasanqua.utils.forge;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.stream.Stream;

public final class CommandUtils {

	public static final ArgumentParser<Integer> INTEGER_ARGUMENT_PARSER = (reader) -> Integer.parseInt(
			reader.advance());
	public static final ArgumentParser<String> STRING_ARGUMENT_PARSER = (reader) -> {
		StringBuilder value = new StringBuilder(reader.advance());
		if (value.charAt(0) != '"' && value.charAt(0) != '\'') {
			return value.toString();
		}
		char quote = value.charAt(0);
		String escape = "\\" + quote;
		while (reader.canAdvance()) {
			String current = reader.advance();
			value.append(" ").append(current);
			if (!current.contains(escape) && value.charAt(value.length() - 1) == quote) {
				break;
			}
		}
		if (!(value.charAt(value.length() - 1) == quote)) {
			throw new RuntimeException("Invalid string argument provided!");
		}
		String result = value.toString().replace(escape, String.valueOf(quote));
		return result.substring(1, result.length() - 1);
	};

	public static final ArgumentParser<WorldServer> WORLD_SERVER_ARGUMENT_PARSER = (reader) -> {
		String world = STRING_ARGUMENT_PARSER.parse(reader);
		return Stream.of(DimensionManager.getWorlds())
				.filter(w -> w.getWorldInfo().getWorldName().equalsIgnoreCase(world))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("World with name " + world + " not found!"));
	};

	public static ArgumentKey.Builder<Integer> integerKeyBuilder(String id) {
		return CommandUtils.<Integer>argumentKeyBuilder().id(id).parser(INTEGER_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<String> stringKeyBuilder(String id) {
		return CommandUtils.<String>argumentKeyBuilder().id(id).parser(STRING_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<WorldServer> worldKeyBuilder(String id) {
		return CommandUtils.<WorldServer>argumentKeyBuilder().id(id).parser(WORLD_SERVER_ARGUMENT_PARSER);
	}

	public static <T> ArgumentKey.Builder<T> argumentKeyBuilder() {
		return new ArgumentKey.Builder<>();
	}

	public static ArgumentContextParser.Builder contextParserBuilder() {
		return new ArgumentContextParser.Builder();
	}

}
