package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PlayerArgumentParser implements ArgumentParser<EntityPlayerMP> {

	@Override
	public EntityPlayerMP parse(ArgumentReader reader) throws Exception {
		return PreconditionUtils.checkNotNull(FMLCommonHandler.instance()
				.getMinecraftServerInstance()
				.getPlayerList()
				.getPlayerByUsername(reader.advance()));
	}

}
