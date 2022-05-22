package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;
import me.sasanqua.utils.forge.CommandUtils;
import net.minecraft.util.math.BlockPos;

public class BlockPosArgumentParser implements ArgumentParser<BlockPos> {

	@Override
	public BlockPos parse(ArgumentReader reader) throws Exception {
		return new BlockPos(CommandUtils.VEC3I_ARGUMENT_PARSER.parse(reader));
	}

}
