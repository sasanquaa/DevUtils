package me.sasanqua.utils.forge;

import net.minecraft.command.CommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentTest {

	final Logger logger = LogManager.getLogger("ArgumentTest");

	@Test
	public void testValidParsing() throws Exception {

		ArgumentKey<Integer> requiredKey1 = CommandUtils.integerKeyBuilder("a").build();
		ArgumentKey<Integer> requiredKey2 = CommandUtils.integerKeyBuilder("b").build();
		ArgumentKey<Integer> optionalKey1 = CommandUtils.integerKeyBuilder("c").flag().build();
		ArgumentKey<Integer> optionalKey2 = CommandUtils.integerKeyBuilder("d").flag().build();

		ArgumentContextParser parser = CommandUtils.contextParserBuilder()
				.add(requiredKey1)
				.add(optionalKey1)
				.add(optionalKey2)
				.add(requiredKey2)
				.build();

		String[] args1 = new String[] { "111", "222" };
		String[] args2 = new String[] { "111", "-c", "333", "-d", "444", "222" };
		String[] args3 = new String[] { "111", "-d", "444", "222" };

		ArgumentContext context1 = parser.parse(args1);
		ArgumentContext context2 = parser.parse(args2);
		ArgumentContext context3 = parser.parse(args3);

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

		ArgumentKey<String> requiredKey1 = CommandUtils.stringKeyBuilder("a").build();

		ArgumentContextParser parser = CommandUtils.contextParserBuilder().add(requiredKey1).build();

		String escape = "\\'";
		String value = "'This     is an\\'\"\"\"    argument test....'";

		String[] args1 = value.split(" ");

		logger.info("Original value: {}", value);
		logger.info("Split value   : {}", Arrays.toString(args1));

		ArgumentContext context = parser.parse(args1);

		logger.info("Parsed value  : {}", context.get(requiredKey1));

		assertEquals(value.substring(1, value.length() - 1).replace(escape, "\'"), context.get(requiredKey1));

		String escape2 = "\\\"";
		String value2 = "\"This     is an''''' '' ' ' '\\\" \\\" \\\"    argument test....\\\"\"";

		String[] args2 = value2.split(" ");

		logger.info("Original value: {}", value2);
		logger.info("Split value   : {}", Arrays.toString(args2));

		ArgumentContext context2 = parser.parse(args2);

		logger.info("Parsed value  : {}", context2.get(requiredKey1));

		assertEquals(value2.substring(1, value2.length() - 1).replace(escape2, "\""), context2.get(requiredKey1));
	}

	@Test
	public void testInvalidParsing() throws Exception {

		ArgumentKey<Integer> requiredKey1 = CommandUtils.integerKeyBuilder("a").build();
		ArgumentKey<Integer> requiredKey2 = CommandUtils.integerKeyBuilder("b").build();
		ArgumentKey<Integer> optionalKey1 = CommandUtils.integerKeyBuilder("c").flag().build();
		ArgumentKey<Integer> optionalKey2 = CommandUtils.integerKeyBuilder("d").flag().build();

		ArgumentContextParser parser = CommandUtils.contextParserBuilder()
				.add(requiredKey1)
				.add(optionalKey1)
				.add(optionalKey2)
				.add(requiredKey2)
				.build();

		String[] args1 = new String[] { "111", "-d", "444", "-c", "333", "222" };
		String[] args2 = new String[] { "111" };
		String[] args3 = new String[] { "111", "-c", "333" };

		assertThrows(Exception.class, () -> parser.parse(args1));
		assertThrows(CommandException.class, () -> parser.parse(args2));
		assertThrows(CommandException.class, () -> parser.parse(args3));

	}

}
