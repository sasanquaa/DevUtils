package me.sasanqua.utils.forge;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.stream.Stream;

public final class CommandUtils {

	public static final ArgumentParser<Boolean> BOOLEAN_ARGUMENT_PARSER = (reader) -> Boolean.parseBoolean(
			(reader.advance()));
	public static final ArgumentParser<Integer> INTEGER_ARGUMENT_PARSER = (reader) -> Integer.parseInt(
			reader.advance());

	public static final ArgumentParser<Double> DOUBLE_ARGUMENT_PARSER = (reader) -> Double.parseDouble(
			reader.advance());
	public static final ArgumentParser<String> STRING_ARGUMENT_PARSER = (reader) -> {
		StringBuilder value = new StringBuilder(reader.advance());
		if (value.charAt(0) != '"' && value.charAt(0) != '\'') {
			return value.toString();
		}
		char quote = value.charAt(0);
		String escape = "\\" + quote;
		while (reader.canAdvance()) {
			String current = reader.advance();
			value.append(" ").append(current);
			if (!current.contains(escape) && value.charAt(value.length() - 1) == quote) {
				break;
			}
		}
		if (!(value.charAt(value.length() - 1) == quote)) {
			throw new RuntimeException("Invalid string argument provided!");
		}
		String result = value.toString().replace(escape, String.valueOf(quote));
		return result.substring(1, result.length() - 1);
	};

	public static final ArgumentParser<WorldServer> WORLD_SERVER_ARGUMENT_PARSER = (reader) -> {
		String world = STRING_ARGUMENT_PARSER.parse(reader);
		return Stream.of(DimensionManager.getWorlds())
				.filter(w -> w.getWorldInfo().getWorldName().equalsIgnoreCase(world))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("World with name " + world + " not found!"));
	};

	public static final ArgumentParser<Vec3i> VEC3I_ARGUMENT_PARSER = (reader) -> {
		int x = INTEGER_ARGUMENT_PARSER.parse(reader);
		int y = INTEGER_ARGUMENT_PARSER.parse(reader);
		int z = INTEGER_ARGUMENT_PARSER.parse(reader);
		return new Vec3i(x, y, z);
	};

	public static final ArgumentParser<Vec3d> VEC3D_ARGUMENT_PARSER = (reader) -> {
		double x = DOUBLE_ARGUMENT_PARSER.parse(reader);
		double y = DOUBLE_ARGUMENT_PARSER.parse(reader);
		double z = DOUBLE_ARGUMENT_PARSER.parse(reader);
		return new Vec3d(x, y, z);
	};

	public static final ArgumentParser<BlockPos> BLOCK_POS_ARGUMENT_PARSER = (reader) -> new BlockPos(
			VEC3I_ARGUMENT_PARSER.parse(reader));

	public static ArgumentKey.Builder<Boolean> booleanKeyBuilder(String id) {
		return CommandUtils.<Boolean>argumentKeyBuilder().id(id).parser(BOOLEAN_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<Integer> integerKeyBuilder(String id) {
		return CommandUtils.<Integer>argumentKeyBuilder().id(id).parser(INTEGER_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<Double> doubleKeyBuilder(String id) {
		return CommandUtils.<Double>argumentKeyBuilder().id(id).parser(DOUBLE_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<String> stringKeyBuilder(String id) {
		return CommandUtils.<String>argumentKeyBuilder().id(id).parser(STRING_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<WorldServer> worldKeyBuilder(String id) {
		return CommandUtils.<WorldServer>argumentKeyBuilder().id(id).parser(WORLD_SERVER_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<Vec3i> vec3iKeyBuilder(String id) {
		return CommandUtils.<Vec3i>argumentKeyBuilder().id(id).parser(VEC3I_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<Vec3d> vec3dKeyBuilder(String id) {
		return CommandUtils.<Vec3d>argumentKeyBuilder().id(id).parser(VEC3D_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<BlockPos> blockPosKeyBuilder(String id) {
		return CommandUtils.<BlockPos>argumentKeyBuilder().id(id).parser(BLOCK_POS_ARGUMENT_PARSER);
	}

	public static <T> ArgumentKey.Builder<T> argumentKeyBuilder() {
		return new ArgumentKey.Builder<>();
	}

	public static ArgumentContextParser.Builder contextParserBuilder() {
		return new ArgumentContextParser.Builder();
	}

}
