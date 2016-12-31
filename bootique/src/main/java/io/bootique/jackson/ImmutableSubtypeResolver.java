package io.bootique.jackson;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;

import java.util.Collection;


/**
 * A hack that allows to prevent modifications to SubtypeResolver outside the initialization code.
 *
 * @since 0.21
 */
public class ImmutableSubtypeResolver<T> extends StdSubtypeResolver {

    private boolean locked;

    public ImmutableSubtypeResolver(Collection<Class<? extends T>> subtypes) {

        Class<?>[] subtypesArray = subtypes.toArray(new Class<?>[subtypes.size()]);
        registerSubtypes(subtypesArray);

        // lock the object against further modification
        locked = true;
    }


    @Override
    public void registerSubtypes(Class<?>... classes) {
        if (locked) {
            throw new UnsupportedOperationException("This object is immutable");
        } else {
            super.registerSubtypes(classes);
        }
    }

    @Override
    public void registerSubtypes(NamedType... types) {
        if (locked) {
            throw new UnsupportedOperationException("This object is immutable");
        } else {
            super.registerSubtypes(types);
        }
    }
}
