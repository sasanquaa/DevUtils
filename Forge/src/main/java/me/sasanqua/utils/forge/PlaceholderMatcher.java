package me.sasanqua.utils.forge;

import java.util.regex.Pattern;

public final class PlaceholderMatcher {

	private final Pattern pattern;
	private final String argumentsSeparator;
	private final String placeholderArgumentsSeparator;

	PlaceholderMatcher(final Pattern pattern, final String argumentsSeparator, final String placeholderArgumentsSeparator) {
		this.pattern = pattern;
		this.argumentsSeparator = argumentsSeparator;
		this.placeholderArgumentsSeparator = placeholderArgumentsSeparator;
	}

	Pattern getPattern() {
		return pattern;
	}

	String getArgumentsSeparator() {
		return argumentsSeparator;
	}

	String getPlaceholderArgumentsSeparator() {
		return placeholderArgumentsSeparator;
	}

}
