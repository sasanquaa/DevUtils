package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.def.ChoicesArgumentParser;
import me.sasanqua.utils.forge.def.EnumArgumentParser;
import me.sasanqua.utils.forge.def.PlayerArgumentParser;
import me.sasanqua.utils.forge.def.WorldArgumentParser;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class TabCompleter {

	private static final Map<Class<?>, Function<ArgumentParser<?>, List<String>>> COMPLETER_MAP = new HashMap<>();

	static {
		COMPLETER_MAP.put(ChoicesArgumentParser.class, (parser) -> ((ChoicesArgumentParser) parser).getChoices());
		COMPLETER_MAP.put(EnumArgumentParser.class, (parser) -> Stream.of(
						PreconditionUtils.checkNotNull(((EnumArgumentParser) parser).getEnumClass().getEnumConstants()))
				.map(Enum.class::cast)
				.map(Enum::name)
				.map(String::toLowerCase)
				.collect(Collectors.toList()));
		COMPLETER_MAP.put(WorldArgumentParser.class, (parser) -> Stream.of(DimensionManager.getWorlds())
				.map(w -> w.getWorldInfo().getWorldName())
				.collect(Collectors.toList()));
		COMPLETER_MAP.put(PlayerArgumentParser.class, (parser) -> Arrays.asList(
				FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames()));
	}

	static List<String> getTabCompletions(ArgumentParser<?> parser) {
		return Optional.ofNullable(COMPLETER_MAP.get(parser.getClass()))
				.map(completer -> completer.apply(parser))
				.orElse(Collections.EMPTY_LIST);
	}

}
