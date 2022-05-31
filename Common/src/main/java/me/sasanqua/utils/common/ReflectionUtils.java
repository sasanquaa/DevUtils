package me.sasanqua.utils.common;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ReflectionUtils {

	private static final Map<Integer, Constructor<?>> constructorCache = new ConcurrentHashMap<>();
	private static final Map<Integer, Field> fieldCache = new ConcurrentHashMap<>();
	private static final Map<Integer, Method> methodCache = new ConcurrentHashMap<>();

	public static <T> Optional<T> newInstance(final Class<T> clazz, final Object... args) {
		final Class<?>[] argsClasses = Stream.of(args)
				.map(arg -> (@NonNull Class<?>) arg.getClass())
				.toArray(Class[]::new);
		final int hash = Objects.hash(clazz, argsClasses);
		Constructor<?> constructor = constructorCache.get(hash);
		try {
			if (constructor == null) {
				constructor = clazz.getConstructor(argsClasses);
				constructor.setAccessible(true);
				constructorCache.putIfAbsent(hash, constructor);
			}
			return Optional.ofNullable((T) constructor.newInstance(args));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static <T> Optional<T> getField(final Object obj, final String fieldName) {
		try {
			return Optional.ofNullable((T) getFieldOrCreate(obj, fieldName).get(obj));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static <T> Optional<T> setField(final Object obj, final String fieldName, final Object value) {
		try {
			final Optional<T> previousValue = getField(obj, fieldName);
			getFieldOrCreate(obj, fieldName).set(obj, value);
			return previousValue;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static <T> Optional<T> invokeMethod(final Object obj, final String methodName, final Object... args) {
		final Class<?>[] argsClasses = Stream.of(args)
				.map(arg -> (@NonNull Class<?>) arg.getClass())
				.toArray(Class[]::new);
		final int hash = Objects.hash(obj.getClass(), methodName, argsClasses);
		Method method = methodCache.get(hash);
		try {
			if (method == null) {
				method = obj.getClass().getDeclaredMethod(methodName, argsClasses);
				method.setAccessible(true);
				methodCache.putIfAbsent(hash, method);
			}
			return Optional.ofNullable((T) method.invoke(obj, args));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	private static Field getFieldOrCreate(final Object obj, final String fieldName)
			throws NoSuchFieldException, IllegalAccessException {
		final int hash = Objects.hash(obj.getClass(), fieldName);
		final int modifiersHash = Objects.hash(Field.class, "modifiers");
		Field field = fieldCache.get(hash);
		if (field == null) {
			field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Field modifiersField = fieldCache.get(modifiersHash);
			if (modifiersField == null) {
				modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				fieldCache.putIfAbsent(modifiersHash, modifiersField);
			}
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			fieldCache.putIfAbsent(hash, field);
		}
		return field;
	}

}
