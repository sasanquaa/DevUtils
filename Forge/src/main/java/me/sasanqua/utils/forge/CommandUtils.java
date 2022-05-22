package me.sasanqua.utils.forge;

import me.sasanqua.utils.forge.def.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public final class CommandUtils {

	public static final ArgumentParser<EntityPlayerMP> PLAYER_ARGUMENT_PARSER = new PlayerArgumentParser();
	public static final ArgumentParser<Boolean> BOOLEAN_ARGUMENT_PARSER = new BooleanArgumentParser();
	public static final ArgumentParser<Integer> INTEGER_ARGUMENT_PARSER = new IntegerArgumentParser();
	public static final ArgumentParser<Double> DOUBLE_ARGUMENT_PARSER = new DoubleArgumentParser();
	public static final ArgumentParser<String> STRING_ARGUMENT_PARSER = new StringArgumentParser();
	public static final ArgumentParser<UUID> UUID_ARGUMENT_PARSER = new UUIDArgumentParser();
	public static final ArgumentParser<WorldServer> WORLD_SERVER_ARGUMENT_PARSER = new WorldArgumentParser();
	public static final ArgumentParser<Vec3i> VEC3I_ARGUMENT_PARSER = new Vec3iArgumentParser();
	public static final ArgumentParser<Vec3d> VEC3D_ARGUMENT_PARSER = new Vec3dArgumentParser();
	public static final ArgumentParser<BlockPos> BLOCK_POS_ARGUMENT_PARSER = new BlockPosArgumentParser();

	public static ArgumentKey.Builder<EntityPlayerMP> playerKeyBuilder(String id) {
		return CommandUtils.<EntityPlayerMP>argumentKeyBuilder().id(id).parser(PLAYER_ARGUMENT_PARSER);
	}

	public static ArgumentKey.Builder<Boolean> booleanKeyBuilder(String id) {
		return CommandUtils.<Boolean>argumentKeyBuilder().id(id).parser(BOOLEAN_ARGUMENT_PARSER);
	}

	public static <T extends Enum> ArgumentKey.Builder<T> enumKeyBuilder(String id, Class<T> enumClass) {
		return CommandUtils.<T>argumentKeyBuilder().id(id).parser(new EnumArgumentParser<>(enumClass));
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

	public static ArgumentKey.Builder<UUID> uuidKeyBuilder(String id) {
		return CommandUtils.<UUID>argumentKeyBuilder().id(id).parser(UUID_ARGUMENT_PARSER);
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
