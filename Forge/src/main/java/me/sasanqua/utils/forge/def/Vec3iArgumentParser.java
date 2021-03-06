package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;
import me.sasanqua.utils.forge.CommandUtils;
import net.minecraft.util.math.Vec3i;

public class Vec3iArgumentParser implements ArgumentParser<Vec3i> {

	@Override
	public Vec3i parse(final ArgumentReader reader) throws Exception {
		final int x = CommandUtils.INTEGER_ARGUMENT_PARSER.parse(reader);
		final int y = CommandUtils.INTEGER_ARGUMENT_PARSER.parse(reader);
		final int z = CommandUtils.INTEGER_ARGUMENT_PARSER.parse(reader);
		return new Vec3i(x, y, z);
	}

}
