package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;

import java.util.UUID;

public class UUIDArgumentParser implements ArgumentParser<UUID> {

	@Override
	public UUID parse(ArgumentReader reader) throws Exception {
		return UUID.fromString(reader.advance());
	}

}
