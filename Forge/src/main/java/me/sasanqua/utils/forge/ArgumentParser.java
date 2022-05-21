package me.sasanqua.utils.forge;

@FunctionalInterface
public interface ArgumentParser<T> {
	T parse(ArgumentReader reader) throws Exception;
}
