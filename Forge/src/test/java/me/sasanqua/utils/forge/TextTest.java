package me.sasanqua.utils.forge;

import org.junit.Assert;
import org.junit.Test;

public class TextTest {

	@Test
	public void placeholderTest() {

		TextUtils.registerPlaceholder("name", context -> context.getAssociation(String.class));
		TextUtils.registerPlaceholder("name2", context -> context.getAssociation(String.class));

		String value = "Hello {name}, {name2}!";
		String parsed = TextUtils.parsePlaceholder(value)
				.add("name", "SASANQUA")
				.add("name2", "SASANQUA")
				.parse()
				.getUnformattedText();

		Assert.assertEquals(value.replace("{name}", "SASANQUA").replace("{name2}", "SASANQUA"), parsed);
	}

}
