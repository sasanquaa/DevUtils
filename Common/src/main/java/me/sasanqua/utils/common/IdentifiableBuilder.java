package me.sasanqua.utils.common;

public interface IdentifiableBuilder<T, B extends Identifiable<T>, R> extends Builder<R> {

	B id(T id);

	R build();

}
