package me.sasanqua.utils.forge;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraftforge.server.command.CommandTreeBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

	final Logger logger = LogManager.getLogger("CommandTest");

	@Test
	public void testValidParsing() throws Exception {

		Argument<Integer> requiredKey1 = CommandUtils.integerKeyBuilder("a").build();
		Argument<Integer> requiredKey2 = CommandUtils.integerKeyBuilder("b").build();
		Argument<Integer> optionalKey1 = CommandUtils.integerKeyBuilder("c").flag().build();
		Argument<Integer> optionalKey2 = CommandUtils.integerKeyBuilder("d").flag().build();

		CommandContextParser parser = new CommandContextParser(
				Sets.newLinkedHashSet(Lists.newArrayList(requiredKey1, optionalKey1, optionalKey2, requiredKey2)));

		String[] args1 = new String[] { "111", "222" };
		String[] args2 = new String[] { "111", "-c", "333", "-d", "444", "222" };
		String[] args3 = new String[] { "111", "-d", "444", "222" };

		CommandContext context1 = parser.parse(args1);
		CommandContext context2 = parser.parse(args2);
		CommandContext context3 = parser.parse(args3);

		assertTrue(context1.find(requiredKey1).isPresent() && context1.get(requiredKey1) == 111);
		assertTrue(context1.find(requiredKey2).isPresent() && context1.get(requiredKey2) == 222);

		assertTrue(context2.find(requiredKey1).isPresent() && context2.get(requiredKey1) == 111);
		assertTrue(context2.find(requiredKey2).isPresent() && context2.get(requiredKey2) == 222);
		assertTrue(context2.find(optionalKey1).isPresent() && context2.get(optionalKey1) == 333);
		assertTrue(context2.find(optionalKey2).isPresent() && context2.get(optionalKey2) == 444);

		assertTrue(context3.find(requiredKey1).isPresent() && context3.get(requiredKey1) == 111);
		assertTrue(context3.find(requiredKey2).isPresent() && context3.get(requiredKey2) == 222);
		assertTrue(!context3.find(optionalKey1).isPresent());
		assertTrue(context3.find(optionalKey2).isPresent() && context3.get(optionalKey2) == 444);

	}

	@Test
	public void testValidStringParsing() throws Exception {

		Argument<String> requiredKey1 = CommandUtils.stringKeyBuilder("a").build();

		CommandContextParser parser = new CommandContextParser(Sets.newHashSet(requiredKey1));

		String escape = "\\'";
		String value = "'This     is an\\'\"\"\"    argument test....'";

		String[] args1 = value.split(" ");

		logger.info("Original value: {}", value);
		logger.info("Split value   : {}", Arrays.toString(args1));

		CommandContext context = parser.parse(args1);

		logger.info("Parsed value  : {}", context.get(requiredKey1));

		assertEquals(value.substring(1, value.length() - 1).replace(escape, "\'"), context.get(requiredKey1));

		String escape2 = "\\\"";
		String value2 = "\"This     is an''''' '' ' ' '\\\" \\\" \\\"    argument test....\\\"\"";

		String[] args2 = value2.split(" ");

		logger.info("Original value: {}", value2);
		logger.info("Split value   : {}", Arrays.toString(args2));

		CommandContext context2 = parser.parse(args2);

		logger.info("Parsed value  : {}", context2.get(requiredKey1));

		assertEquals(value2.substring(1, value2.length() - 1).replace(escape2, "\""), context2.get(requiredKey1));
	}

	@Test
	public void testInvalidParsing() throws Exception {

		Argument<Integer> requiredKey1 = CommandUtils.integerKeyBuilder("a").build();
		Argument<Integer> requiredKey2 = CommandUtils.integerKeyBuilder("b").build();
		Argument<Integer> optionalKey1 = CommandUtils.integerKeyBuilder("c").flag().build();
		Argument<Integer> optionalKey2 = CommandUtils.integerKeyBuilder("d").flag().build();

		CommandContextParser parser = new CommandContextParser(
				Sets.newLinkedHashSet(Lists.newArrayList(requiredKey1, optionalKey1, optionalKey2, requiredKey2)));

		String[] args1 = new String[] { "111", "-d", "444", "-c", "333", "222" };
		String[] args2 = new String[] { "111" };
		String[] args3 = new String[] { "111", "-c", "333" };

		assertThrows(Exception.class, () -> parser.parse(args1));
		assertThrows(CommandException.class, () -> parser.parse(args2));
		assertThrows(CommandException.class, () -> parser.parse(args3));

	}

	@Test
	public void testCommandTree() throws Exception {

		CommandSpec innerChild1 = CommandUtils.commandSpecBuilder()
				.permission("innerchild1")
				.addArgument(CommandUtils.integerKeyBuilder("a").build())
				.executor(context -> {
				})
				.build();
		CommandSpec child1 = CommandUtils.commandSpecBuilder()
				.permission("child1")
				.addChild(innerChild1, "i1")
				.executor(context -> {
				})
				.build();

		CommandSpec child2 = CommandUtils.commandSpecBuilder()
				.permission("child2")
				.addArgument(CommandUtils.integerKeyBuilder("b").build())
				.executor(context -> {
				})
				.build();

		CommandSpec first = CommandUtils.commandSpecBuilder().permission("first").executor(context -> {
		}).addChild(child1, "c1").addChild(child2, "c2").build();

		CommandTreeBase base = (CommandTreeBase) CommandUtils.asCommand(first, "f1");

		logger.info("Base command: {}", base.getName());

		for (ICommand command : base.getSubCommands()) {
			logger.info("Sub command: {}", command.getName());

			if (command.getName().equalsIgnoreCase("c1")) {
				CommandTreeBase innerBase = (CommandTreeBase) command;

				for (ICommand c : innerBase.getSubCommands()) {
					logger.info("Inner sub command: {}", c.getName());
				}
			}
		}
	}

	enum TestEnum {
		ENUM1, ENUM2, ENUM3;
	}

	@Test
	public void testTabCompletions() throws Exception {
		CommandSpec commandSpec = CommandUtils.commandSpecBuilder()
				.permission("abc")
				.executor(context -> {
				})
				.addArgument(CommandUtils.choicesKeyBuilder("a", "choice1", "choice 2", "choice    3").build())
				.addArgument(CommandUtils.enumKeyBuilder("b", TestEnum.class).build())
				.build();

		assertEquals(commandSpec.getParser().getTabCompletions(new String[0]),
				Lists.newArrayList("choice1", "choice 2", "choice    3"));
		assertEquals(commandSpec.getParser().getTabCompletions(new String[] { "choice1" }),
				Lists.newArrayList("enum1", "enum2", "enum3"));
	}

}
