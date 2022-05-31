package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;

public class BooleanArgumentParser implements ArgumentParser<Boolean> {

	@Override
	public Boolean parse(final ArgumentReader reader) throws Exception {
		return Boolean.parseBoolean(reader.advance());
	}

}
