package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;
import me.sasanqua.utils.forge.CommandUtils;
import net.minecraft.util.math.Vec3d;

public class Vec3dArgumentParser implements ArgumentParser<Vec3d> {

	@Override
	public Vec3d parse(ArgumentReader reader) throws Exception {
		double x = CommandUtils.DOUBLE_ARGUMENT_PARSER.parse(reader);
		double y = CommandUtils.DOUBLE_ARGUMENT_PARSER.parse(reader);
		double z = CommandUtils.DOUBLE_ARGUMENT_PARSER.parse(reader);
		return new Vec3d(x, y, z);
	}

}
