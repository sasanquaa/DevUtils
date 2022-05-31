package me.sasanqua.utils.sponge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TextUtils {

	public static Component of(final String text) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
	}

	public static Component ofLegacy(final String text) {
		return LegacyComponentSerializer.legacySection().deserialize(text);
	}

	public static String serialize(final Component text) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(text);
	}

	public static String serializeLegacy(final Component text) {
		return LegacyComponentSerializer.legacySection().serialize(text);
	}

	public static List<Component> lore(final String... texts) {
		return lore(Arrays.asList(texts));
	}

	public static List<Component> lore(final List<String> texts) {
		return texts.stream().map(TextUtils::of).collect(Collectors.toList());
	}

	public static List<Component> loreLegacy(final String... texts) {
		return loreLegacy(Arrays.asList(texts));
	}

	public static List<Component> loreLegacy(final List<String> texts) {
		return texts.stream().map(TextUtils::ofLegacy).collect(Collectors.toList());
	}

}
