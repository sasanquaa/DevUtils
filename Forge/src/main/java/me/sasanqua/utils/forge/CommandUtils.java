package me.sasanqua.utils.forge;

import me.sasanqua.utils.common.PreconditionUtils;
import me.sasanqua.utils.forge.def.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.toposort.TopologicalSort;
import net.minecraftforge.server.command.CommandTreeBase;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Supplier;

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

	public static Argument.Builder<EntityPlayerMP> playerKeyBuilder(String id) {
		return CommandUtils.<EntityPlayerMP>argumentKeyBuilder().id(id).parser(PLAYER_ARGUMENT_PARSER);
	}

	public static Argument.Builder<Boolean> booleanKeyBuilder(String id) {
		return CommandUtils.<Boolean>argumentKeyBuilder().id(id).parser(BOOLEAN_ARGUMENT_PARSER);
	}

	public static <T extends Enum> Argument.Builder<T> enumKeyBuilder(String id, Class<T> enumClass) {
		return CommandUtils.<T>argumentKeyBuilder().id(id).parser(new EnumArgumentParser<>(enumClass));
	}

	public static Argument.Builder<String> choicesKeyBuilder(String id, String... choices) {
		List<String> choicesList = Arrays.asList(choices);
		return choicesKeyBuilder(id, () -> choicesList);
	}

	public static Argument.Builder<String> choicesKeyBuilder(String id, Supplier<List<String>> choicesSupplier) {
		return CommandUtils.<String>argumentKeyBuilder().id(id).parser(new ChoicesArgumentParser(choicesSupplier));
	}

	public static Argument.Builder<Integer> integerKeyBuilder(String id) {
		return CommandUtils.<Integer>argumentKeyBuilder().id(id).parser(INTEGER_ARGUMENT_PARSER);
	}

	public static Argument.Builder<Double> doubleKeyBuilder(String id) {
		return CommandUtils.<Double>argumentKeyBuilder().id(id).parser(DOUBLE_ARGUMENT_PARSER);
	}

	public static Argument.Builder<String> stringKeyBuilder(String id) {
		return CommandUtils.<String>argumentKeyBuilder().id(id).parser(STRING_ARGUMENT_PARSER);
	}

	public static Argument.Builder<UUID> uuidKeyBuilder(String id) {
		return CommandUtils.<UUID>argumentKeyBuilder().id(id).parser(UUID_ARGUMENT_PARSER);
	}

	public static Argument.Builder<WorldServer> worldKeyBuilder(String id) {
		return CommandUtils.<WorldServer>argumentKeyBuilder().id(id).parser(WORLD_SERVER_ARGUMENT_PARSER);
	}

	public static Argument.Builder<Vec3i> vec3iKeyBuilder(String id) {
		return CommandUtils.<Vec3i>argumentKeyBuilder().id(id).parser(VEC3I_ARGUMENT_PARSER);
	}

	public static Argument.Builder<Vec3d> vec3dKeyBuilder(String id) {
		return CommandUtils.<Vec3d>argumentKeyBuilder().id(id).parser(VEC3D_ARGUMENT_PARSER);
	}

	public static Argument.Builder<BlockPos> blockPosKeyBuilder(String id) {
		return CommandUtils.<BlockPos>argumentKeyBuilder().id(id).parser(BLOCK_POS_ARGUMENT_PARSER);
	}

	public static <T> Argument.Builder<T> argumentKeyBuilder() {
		return new Argument.Builder<>();
	}

	public static CommandSpec.Builder commandSpecBuilder() {
		return new CommandSpec.Builder();
	}

	public static CommandBase asCommand(CommandSpec spec, String... keys) {
		PreconditionUtils.checkArgument(keys.length > 0, "Keys must not be empty");
		Set<CommandSpec> topVisitedSet = new HashSet<>();
		Stack<CommandSpec> topStack = new Stack<>();
		topStack.push(spec);

		while (!topStack.isEmpty()) {
			CommandSpec topCurrentSpec = topStack.pop();
			if (topVisitedSet.contains(topCurrentSpec)) {
				continue;
			}
			topVisitedSet.add(topCurrentSpec);
			topCurrentSpec.getChildren().keySet().forEach(topStack::add);

			Set<CommandSpec> visitedSet = new HashSet<>();
			Set<CommandSpec> stackSet = new HashSet<>();
			Stack<CommandSpec> stack = new Stack<>();
			stack.push(topCurrentSpec);

			while (!stack.isEmpty()) {
				CommandSpec currentSpec = stack.peek();
				if (!visitedSet.contains(currentSpec)) {
					visitedSet.add(currentSpec);
					stackSet.add(currentSpec);
				} else {
					stackSet.remove(currentSpec);
					stack.pop();
				}
				for (CommandSpec childSpec : currentSpec.getChildren().keySet()) {
					if (!visitedSet.contains(childSpec)) {
						stack.push(childSpec);
					} else {
						PreconditionUtils.checkArgument(!stackSet.contains(childSpec),
								"Cannot build commands with cyclic dependency");
					}
				}
			}
		}

		return asCommandImpl(spec, keys);
	}

	private static CommandBase asCommandImpl(CommandSpec spec, String... keys) {
		if (spec.getChildren().isEmpty()) {
			return new CommandBaseWrapper(spec, keys);
		}
		CommandTreeBaseWrapper command = new CommandTreeBaseWrapper(spec, keys);
		spec.getChildren().asMap().forEach((k, v) -> command.addSubcommand(asCommandImpl(k, v.toArray(new String[0]))));
		return command;
	}

	private static boolean hasCycle(TopologicalSort.DirectedGraph<CommandSpec> graph, CommandSpec spec, Set<CommandSpec> visitedSet, Set<CommandSpec> recursionSet) {
		if (recursionSet.contains(spec)) {
			return true;
		}
		if (visitedSet.contains(spec)) {
			return false;
		}
		visitedSet.add(spec);
		recursionSet.add(spec);
		for (CommandSpec commandSpec : graph.edgesFrom(spec)) {
			if (hasCycle(graph, commandSpec, visitedSet, recursionSet)) {
				return true;
			}
		}
		recursionSet.remove(spec);
		return false;
	}

	private static class CommandBaseWrapper extends CommandBase {

		final CommandSpec spec;
		final List<String> keys;

		CommandBaseWrapper(CommandSpec spec, String[] keys) {
			this.spec = spec;
			this.keys = Collections.unmodifiableList(Arrays.asList(keys));
		}

		@Override
		public String getName() {
			return keys.get(0);
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getListOfStringsMatchingLastWord(args, spec.getParser().getTabCompletions(args));
		}

		@Override
		public String getUsage(ICommandSender sender) {
			String usage = spec.getParser().getUsage();
			return spec.getUsage().orElse(usage.isEmpty() ? "" : getName() + " arguments: " + usage);
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			CommandContext context = spec.getParser().parse(args);
			context.setSender(sender);
			context.setServer(server);
			spec.getExecutor().execute(context);
		}

		@Override
		public List<String> getAliases() {
			return keys;
		}

		@Override
		public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
			return sender.canUseCommand(this.getRequiredPermissionLevel(), spec.getPermission());
		}

	}

	private static class CommandTreeBaseWrapper extends CommandTreeBase {

		final CommandSpec spec;
		final List<String> keys;

		CommandTreeBaseWrapper(CommandSpec spec, String[] keys) {
			this.spec = spec;
			this.keys = Collections.unmodifiableList(Arrays.asList(keys));
		}

		@Override
		public String getName() {
			return keys.get(0);
		}

		@Override
		public String getUsage(ICommandSender sender) {
			String usage = spec.getParser().getUsage();
			return spec.getUsage().orElse(usage.isEmpty() ? "" : getName() + " arguments: " + usage);
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
			return getListOfStringsMatchingLastWord(args, spec.getParser().getTabCompletions(args));
		}

		@Override
		public List<String> getAliases() {
			return keys;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			if (args.length < 1) {
				CommandContext context = spec.getParser().parse(args);
				context.setSender(sender);
				context.setServer(server);
				spec.getExecutor().execute(context);
			} else {
				ICommand cmd = getSubCommand(args[0]);
				if (cmd == null) {
					String subCommandsString = getAvailableSubCommandsString(server, sender);
					throw new CommandException("commands.tree_base.invalid_cmd.list_subcommands", args[0],
							subCommandsString);
				} else if (!cmd.checkPermission(server, sender)) {
					throw new CommandException("commands.generic.permission");
				} else {
					cmd.execute(server, sender, shiftArgs(args));
				}
			}
		}

		@Override
		public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
			return sender.canUseCommand(getRequiredPermissionLevel(), spec.getPermission());
		}

		private String getAvailableSubCommandsString(MinecraftServer server, ICommandSender sender) {
			Collection<String> availableCommands = new ArrayList<>();
			for (ICommand command : getSubCommands()) {
				if (command.checkPermission(server, sender)) {
					availableCommands.add(command.getName());
				}
			}
			return CommandBase.joinNiceStringFromCollection(availableCommands);
		}

		private static String[] shiftArgs(@Nullable String[] s) {
			if (s == null || s.length == 0) {
				return new String[0];
			}
			String[] s1 = new String[s.length - 1];
			System.arraycopy(s, 1, s1, 0, s1.length);
			return s1;
		}

	}

}
