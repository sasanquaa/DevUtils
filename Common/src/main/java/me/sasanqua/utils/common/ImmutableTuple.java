package me.sasanqua.utils.common;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

public final class ImmutableTuple<K, V> {

	private final K first;
	private final V second;
	private final BiFunction<K, V, Integer> hashCodeFunction;
	private final TriFunction<K, V, Object, Boolean> equalsFunction;

	ImmutableTuple(K first, V second, BiFunction<K, V, Integer> hashCodeFunction, TriFunction<K, V, Object, Boolean> equalsFunction) {
		this.first = first;
		this.second = second;
		this.hashCodeFunction = hashCodeFunction;
		this.equalsFunction = equalsFunction;
	}

	public K getFirst() {
		return this.first;
	}

	public V getSecond() {
		return this.second;
	}

	@Override
	public int hashCode() {
		return this.hashCodeFunction.apply(first, second);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == null) {
			return false;
		}
		return this.equalsFunction.apply(first, second, obj);
	}

	public static <K, V> ImmutableTuple<K, V> of(K first, V second) {
		return of(first, second, (k, v) -> Objects.hash(k, v),
				(k, v, obj) -> obj instanceof ImmutableTuple && obj.hashCode() == Objects.hash(k, v));
	}

	public static <K, V> ImmutableTuple<K, V> of(K first, V second, BiFunction<K, V, Integer> hashCodeFunction, TriFunction<K, V, Object, Boolean> equalsFunction) {
		return new ImmutableTuple<>(first, second, hashCodeFunction, equalsFunction);
	}

}
