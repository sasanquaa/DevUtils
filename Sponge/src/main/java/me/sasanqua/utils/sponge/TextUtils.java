package me.sasanqua.utils.sponge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TextUtils {

	public static Component of(String str) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(str);
	}

	public static String serialize(Component text) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(text);
	}

	public static Component ofLegacy(String str) {
		return LegacyComponentSerializer.legacySection().deserialize(str);
	}

	public static String serializeLegacy(Component text) {
		return LegacyComponentSerializer.legacySection().serialize(text);
	}

	public static List<Component> lore(String... msgs) {
		return lore(Arrays.asList(msgs));
	}

	public static List<Component> lore(List<String> msgs) {
		return msgs.stream().map(TextUtils::of).collect(Collectors.toList());
	}

	public static List<Component> loreLegacy(String... msgs) {
		return loreLegacy(Arrays.asList(msgs));
	}

	public static List<Component> loreLegacy(List<String> msgs) {
		return msgs.stream().map(TextUtils::ofLegacy).collect(Collectors.toList());
	}

}
