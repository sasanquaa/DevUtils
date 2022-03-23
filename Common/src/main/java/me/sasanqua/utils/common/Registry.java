package me.sasanqua.utils.common;

import java.util.Collection;
import java.util.Optional;

public interface Registry<T extends Identifiable> {

	void register(T item);

	void unregister(T item);

	Optional<T> get(String id);

	Collection<T> getAll();

}
