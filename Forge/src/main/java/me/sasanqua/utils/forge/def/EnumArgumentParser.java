package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;

import java.util.stream.Stream;

public class EnumArgumentParser<T extends Enum<T>> implements ArgumentParser<T> {

	private final Class<T> enumClass;

	public EnumArgumentParser(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public T parse(ArgumentReader reader) throws Exception {
		String value = reader.advance();
		return Stream.of(PreconditionUtils.checkNotNull(enumClass.getEnumConstants()))
				.filter(t -> t.name().equalsIgnoreCase(value))
				.findFirst()
				.get();
	}

}
