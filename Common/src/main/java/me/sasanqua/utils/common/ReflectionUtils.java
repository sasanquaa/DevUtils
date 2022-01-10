package me.sasanqua.utils.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ReflectionUtils {

	private static final ConcurrentHashMap<Integer, Constructor<?>> constructorCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, Field> fieldCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, Method> methodCache = new ConcurrentHashMap<>();

	public static <T> Optional<T> newInstance(Class<T> clazz, Object... args) {
		Class<?>[] argsClasses = new Class<?>[0];
		if (args != null) {
			argsClasses = Stream.of(args).map(arg -> arg.getClass()).toArray(Class[]::new);
		}
		int hash = Objects.hash(clazz, argsClasses);
		Constructor<?> constructor = constructorCache.get(hash);
		try {
			if (constructor == null) {
				constructor = clazz.getConstructor(argsClasses);
				constructor.setAccessible(true);
				constructorCache.putIfAbsent(hash, constructor);
			}
			return Optional.ofNullable((T) constructor.newInstance(args));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static <T> Optional<T> getField(Object obj, String fieldName) {
		int hash = Objects.hash(obj.getClass().hashCode(), fieldName.hashCode());
		Field field = fieldCache.get(hash);
		try {
			if (field == null) {
				field = obj.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				fieldCache.putIfAbsent(hash, field);
			}
			return Optional.ofNullable((T) field.get(obj));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static <T> Optional<T> invokeMethod(Object obj, String methodName, Object... args) {
		Class<?>[] argsClasses = new Class<?>[0];
		if (args != null) {
			argsClasses = Stream.of(args).map(arg -> arg.getClass()).toArray(Class[]::new);
		}
		int hash = Objects.hash(obj.getClass(), methodName, argsClasses);
		Method method = methodCache.get(hash);
		try {
			if (method == null) {
				method = obj.getClass().getDeclaredMethod(methodName, argsClasses);
				method.setAccessible(true);
				methodCache.putIfAbsent(hash, method);
			}
			return Optional.ofNullable((T) method.invoke(obj, args));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

}
