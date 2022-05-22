package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;
import me.sasanqua.utils.forge.CommandUtils;

import java.util.Collections;
import java.util.List;

public class ChoicesArgumentParser implements ArgumentParser<String> {

	private final List<String> choices;

	public ChoicesArgumentParser(List<String> choices) {
		this.choices = Collections.unmodifiableList(choices);
	}

	public List<String> getChoices() {
		return choices;
	}

	@Override
	public String parse(ArgumentReader reader) throws Exception {
		String value = CommandUtils.STRING_ARGUMENT_PARSER.parse(reader);
		PreconditionUtils.checkState(choices.contains(value));
		return value;
	}

}
