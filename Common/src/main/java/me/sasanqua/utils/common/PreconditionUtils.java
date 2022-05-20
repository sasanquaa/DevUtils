package me.sasanqua.utils.common;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class PreconditionUtils {

	public static void checkArgument(boolean expression) {
		if (!expression) {
			throw new IllegalArgumentException();
		}
	}

	public static void checkArgument(boolean expression, @Nullable Object message) {
		if (!expression) {
			throw new IllegalArgumentException(String.valueOf(message));
		}
	}

	public static <T> T checkNotNull(@Nullable T value) {
		if (value == null) {
			throw new NullPointerException();
		}
		return value;
	}

	public static <T> T checkNotNull(@Nullable T value, @Nullable Object message) {
		if (value == null) {
			throw new NullPointerException(String.valueOf(message));
		}
		return value;
	}

	public static void checkState(boolean state) {
		if (!state) {
			throw new IllegalStateException();
		}
	}

	public static void checkState(boolean state, @Nullable String message) {
		if (!state) {
			throw new IllegalStateException(String.valueOf(message));
		}
	}

}
