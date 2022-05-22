package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;

public class DoubleArgumentParser implements ArgumentParser<Double> {

	@Override
	public Double parse(ArgumentReader reader) throws Exception {
		return Double.parseDouble(reader.advance());
	}

}
