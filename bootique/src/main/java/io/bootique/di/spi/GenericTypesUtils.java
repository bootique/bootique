package io.bootique.di.spi;

import io.bootique.di.TypeLiteral;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class GenericTypesUtils {

    static Type getGenericParameterType(Type type) {
        if (type instanceof ParameterizedType pt) {
            Type[] parameters = pt.getActualTypeArguments();

            if (parameters.length == 1) {
                return parameters[0];
            }
        }

        return null;
    }

    static Class<?> parameterClass(Type type) {
        Type parameterType = getGenericParameterType(type);
        if (parameterType == null) {
            return null;
        }

        return typeToClass(parameterType);
    }

    private static Class<?> typeToClass(Type type) {
        if (type instanceof Class c) {
            return c;
        } else if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        } else {
            return Object.class;
        }
    }

    /**
     * Resolves variable type (which is essentially providing only the name, like T) of the field to the actual type
     * by looking up generic type info in the class hierarchy.
     *
     * @param objectClass  class that causes this field resolution (not the class declaring field)
     * @param field        we are trying to resolve
     * @param variableType variable type
     * @return actual type or null if there's something missing
     */
    static TypeLiteral<?> resolveVariableType(Class<?> objectClass, Field field, Type variableType) {
        Class<?> declaringClass = field.getDeclaringClass();

        // traverse the class hierarchy to find the superclass that defines type variable we are trying to solve
        Type genericSuperclass = getSuperclassDeclaringField(objectClass, declaringClass);
        if (genericSuperclass == null) {
            return null;
        }

        // get index of the variable type we are looking for
        int idx = getTypeVariableIdx(variableType.getTypeName(), declaringClass);
        if (idx == -1) {
            return null;
        }

        ParameterizedType parameterizedSuperType = (ParameterizedType) genericSuperclass;
        Type actualType = parameterizedSuperType.getActualTypeArguments()[idx];
        return TypeLiteral.of(actualType);
    }

    static private Type getSuperclassDeclaringField(Class<?> objectClass, Class<?> declaringClass) {
        while (objectClass.getSuperclass() != null && !objectClass.getSuperclass().equals(declaringClass)) {
            objectClass = objectClass.getSuperclass();
        }
        Type genericSuperclass = objectClass.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return null;
        }
        return genericSuperclass;
    }

    /**
     * @param typeVariableName name of the generic's type variable
     * @param declaringClass   generic class, that defines that type variable
     * @return index of the typeVariableName
     */
    static private int getTypeVariableIdx(String typeVariableName, Class<?> declaringClass) {
        TypeVariable<? extends Class<?>>[] typeParameters = declaringClass.getTypeParameters();
        int idx = 0;
        for (; idx < typeParameters.length; idx++) {
            if (typeParameters[idx].getName().equals(typeVariableName)) {
                break;
            }
        }
        if (idx == typeParameters.length) {
            return -1;
        }
        return idx;
    }
}
