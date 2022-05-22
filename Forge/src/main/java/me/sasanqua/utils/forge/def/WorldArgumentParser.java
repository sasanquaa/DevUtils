package me.sasanqua.utils.forge.def;

import me.sasanqua.utils.forge.ArgumentParser;
import me.sasanqua.utils.forge.ArgumentReader;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.stream.Stream;

public class WorldArgumentParser implements ArgumentParser<WorldServer> {

	@Override
	public WorldServer parse(ArgumentReader reader) throws Exception {
		String world = reader.advance();
		return Stream.of(DimensionManager.getWorlds())
				.filter(w -> w.getWorldInfo().getWorldName().equalsIgnoreCase(world))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("World with name " + world + " not found!"));
	}

}
