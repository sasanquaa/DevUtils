package me.sasanqua.utils.forge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;

import java.util.*;
import java.util.stream.Collectors;

public final class PlaceholderContext {

	private final List<String> arguments;
	private final ListMultimap<Class<?>, Object> contextObjects;

	private PlaceholderContext(final PlaceholderContextBuilder builder) {
		this.arguments = Collections.unmodifiableList(builder.arguments);
		this.contextObjects = Multimaps.unmodifiableListMultimap(builder.contextObjects);
	}

	public List<String> getArguments() {
		return arguments;
	}

	public <T> Optional<T> getAssociation(final Class<T> clazz) {
		return contextObjects.get(clazz).stream().findFirst().map(clazz::cast);
	}

	public <T> List<T> getAllAssociations(final Class<T> clazz) {
		return contextObjects.get(clazz)
				.stream()
				.filter(Objects::nonNull)
				.map(clazz::cast)
				.collect(Collectors.toList());
	}

	static final class PlaceholderContextBuilder {

		private List<String> arguments = new ArrayList<>();
		private final ListMultimap<Class<?>, Object> contextObjects = ArrayListMultimap.create();

		PlaceholderContextBuilder arguments(final String... arguments) {
			this.arguments = Lists.newArrayList(arguments);
			return this;
		}

		PlaceholderContextBuilder objects(final Object... objects) {
			for (final Object object : objects) {
				contextObjects.put(object.getClass(), object);
			}
			return this;
		}

		PlaceholderContext build() {
			return new PlaceholderContext(this);
		}

	}

}
