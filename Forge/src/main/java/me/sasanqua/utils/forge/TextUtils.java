package me.sasanqua.utils.forge;

import net.minecraft.util.text.TextComponentString;

public final class TextUtils {

	public static String toLegacy(String str) {
		return str.replace("&", "§");
	}

	public static String toModern(String str) {
		return str.replace("§", "&");
	}

	public static TextComponentString deserialize(String str) {
		return new TextComponentString(toLegacy(str));
	}

	public static TextComponentString serialize(String str) {
		return new TextComponentString(toModern(str));
	}

}
