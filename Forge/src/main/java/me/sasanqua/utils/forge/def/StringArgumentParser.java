package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;

public class StringArgumentParser implements ArgumentParser<String> {

	@Override
	public String parse(ArgumentReader reader) throws Exception {
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
	}

}
