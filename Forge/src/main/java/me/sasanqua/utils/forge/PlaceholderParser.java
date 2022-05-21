package me.sasanqua.utils.forge;

import java.util.Optional;

@FunctionalInterface
public interface PlaceholderParser {
	Optional<String> parse(PlaceholderContext context);

}
