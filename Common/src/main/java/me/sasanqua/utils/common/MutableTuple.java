package me.sasanqua.utils.common;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class MutableTuple<K, V> {

	private K first;
	private V second;
	private final BiFunction<K, V, Integer> hashCodeFunction;
	private final TriFunction<K, V, Object, Boolean> equalsFunction;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	MutableTuple(K first, V second, BiFunction<K, V, Integer> hashCodeFunction, TriFunction<K, V, Object, Boolean> equalsFunction) {
		this.first = first;
		this.second = second;
		this.hashCodeFunction = hashCodeFunction;
		this.equalsFunction = equalsFunction;
	}

	public void getFirst(Consumer<K> consumer) {
		lock.readLock().lock();
		try {
			consumer.accept(this.first);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void getSecond(Consumer<V> consumer) {
		lock.readLock().lock();
		try {
			consumer.accept(this.second);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void get(BiConsumer<K, V> consumer) {

		lock.readLock().lock();
		try {
			consumer.accept(this.first, this.second);
		} finally {
			lock.readLock().unlock();
		}
	}

	public <R> R applyFirst(Function<K, R> function) {
		lock.readLock().lock();
		try {
			return function.apply(this.first);
		} finally {
			lock.readLock().unlock();
		}
	}

	public <R> R applySecond(Function<V, R> function) {
		lock.readLock().lock();
		try {
			return function.apply(this.second);
		} finally {
			lock.readLock().unlock();
		}
	}

	public <R> R apply(BiFunction<K, V, R> function) {
		lock.readLock().lock();
		try {
			return function.apply(this.first, this.second);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setFirst(K first) {
		lock.writeLock().lock();
		try {
			this.first = first;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void setSecond(V second) {
		lock.writeLock().lock();
		try {
			this.second = second;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void set(K first, V second) {
		lock.writeLock().lock();
		try {
			this.first = first;
			this.second = second;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public ImmutableTuple<K, V> toImmutable() {
		lock.readLock().lock();
		try {
			return ImmutableTuple.of(first, second, hashCodeFunction, equalsFunction);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int hashCode() {
		lock.readLock().lock();
		try {
			return this.hashCodeFunction.apply(first, second);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean equals(Object obj) {
		lock.readLock().lock();
		try {
			return this.equalsFunction.apply(first, second, obj);
		} finally {
			lock.readLock().unlock();
		}
	}

	public static <K, V> MutableTuple<K, V> of(K first, V second) {
		return of(first, second, (k, v) -> Objects.hash(k, v),
				(k, v, obj) -> obj instanceof MutableTuple && obj.hashCode() == Objects.hash(k, v, obj));
	}

	public static <K, V> MutableTuple<K, V> of(K first, V second, BiFunction<K, V, Integer> hashCodeFunction, TriFunction<K, V, Object, Boolean> equalsFunction) {
		return new MutableTuple<>(first, second, hashCodeFunction, equalsFunction);
	}

}
