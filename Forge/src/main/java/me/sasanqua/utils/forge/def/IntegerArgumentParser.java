package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;

public class IntegerArgumentParser implements ArgumentParser<Integer> {

	@Override
	public Integer parse(ArgumentReader reader) throws Exception {
		return Integer.parseInt(reader.advance());
	}

}
