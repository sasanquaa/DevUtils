package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;

public class StringArgumentParser implements ArgumentParser<String> {

	@Override
	public String parse(final ArgumentReader reader) throws Exception {
		final StringBuilder value = new StringBuilder(reader.advance());
		if (value.charAt(0) != '"' && value.charAt(0) != '\'') {
			return value.toString();
		}
		final char quote = value.charAt(0);
		final String escape = "\\" + quote;
		while (reader.canAdvance()) {
			final String current = reader.advance();
			value.append(" ").append(current);
			if (!current.contains(escape) && value.charAt(value.length() - 1) == quote) {
				break;
			}
		}
		PreconditionUtils.checkState(value.charAt(value.length() - 1) == quote);
		final String result = value.toString().replace(escape, String.valueOf(quote));
		return result.substring(1, result.length() - 1);
	}

}
