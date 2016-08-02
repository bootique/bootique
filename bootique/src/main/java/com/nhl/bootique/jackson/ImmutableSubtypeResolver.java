package com.nhl.bootique.jackson;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;

import java.util.Collection;


/**
 * A hack that allows to prevent modifications to SubtypeResolver outside the initialization code.
 */
class ImmutableSubtypeResolver extends StdSubtypeResolver {

    private boolean locked;

    ImmutableSubtypeResolver(Collection<Class<?>> subtypes) {

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
