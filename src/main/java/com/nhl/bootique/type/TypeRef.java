package com.nhl.bootique.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A class intended for subclassing for the purpose of obtaining a {@link Type}
 * instance describing a specific generic type.
 * 
 * <p>
 * Inspired by similar Guice and Jackson constructs that work around java
 * generics inference limitations.
 * 
 * @since 0.10
 */
public abstract class TypeRef<T> {

	protected Type type;

	protected TypeRef() {

		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) {
			// should not happen
			throw new IllegalArgumentException("TypeRef constructed without actual type information");
		}

		this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	public Type getType() {
		return type;
	}
}
