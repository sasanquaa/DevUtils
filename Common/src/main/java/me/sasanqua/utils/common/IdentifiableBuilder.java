package me.sasanqua.utils.common;

public interface IdentifiableBuilder<B, R> extends Builder<R> {

	B id(String id);

	R build();

}
