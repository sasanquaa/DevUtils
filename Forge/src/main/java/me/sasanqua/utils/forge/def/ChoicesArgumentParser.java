package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;
import me.sasanqua.utils.forge.CommandUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ChoicesArgumentParser implements ArgumentParser<String> {

	private final Supplier<List<String>> choices;

	public ChoicesArgumentParser(final Supplier<List<String>> supplier) {
		this.choices = supplier;
	}

	public List<String> getChoices() {
		return Collections.unmodifiableList(choices.get());
	}

	@Override
	public String parse(final ArgumentReader reader) throws Exception {
		final String value = CommandUtils.STRING_ARGUMENT_PARSER.parse(reader);
		PreconditionUtils.checkState(getChoices().contains(value));
		return value;
	}

}
