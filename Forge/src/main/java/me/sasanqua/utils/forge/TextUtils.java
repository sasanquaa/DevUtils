package me.sasanqua.utils.forge;

import net.minecraft.util.text.StringTextComponent;

public final class TextUtils {

	public static String toLegacy(String str) {
		return str.replace("&", "§");
	}

	public static String toModern(String str) {
		return str.replace("§", "&");
	}

	public static StringTextComponent deserialize(String str) {
		return new StringTextComponent(toLegacy(str));
	}

	public static StringTextComponent serialize(String str) {
		return new StringTextComponent(toModern(str));
	}

}
