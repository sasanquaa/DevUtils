package me.sasanqua.utils.forge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextTest {

	@Test
	public void placeholderTest() {

		TextUtils.registerPlaceholder("name", context -> context.getAssociation(String.class));
		TextUtils.registerPlaceholder("name2", context -> context.getAssociation(String.class));

		String value = "Hello {name}, {name2}, {name3}!";
		String parsed = TextUtils.parsePlaceholder(value)
				.add("name", "SASANQUA")
				.add("name2", "SASANQUA")
				.parse()
				.getText();

		Assertions.assertEquals(value.replace("{name}", "SASANQUA").replace("{name2}", "SASANQUA"), parsed);
	}

}
