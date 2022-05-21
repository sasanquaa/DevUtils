package me.sasanqua.utils.forge;

public final class CommandUtils {

	public static final ArgumentParser<Integer> INTEGER_ARGUMENT_PARSER = (reader) -> Integer.parseInt(
			reader.advance());

	public static ArgumentKey.Builder integerKeyBuilder(String id) {
		return argumentKeyBuilder().id(id).parser(INTEGER_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder argumentKeyBuilder() {
		return new ArgumentKey.Builder();
	}

	public static ArgumentContextParser.Builder contextParserBuilder() {
		return new ArgumentContextParser.Builder();
	}

}
