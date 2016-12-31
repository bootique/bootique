package io.bootique.jackson;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;


/**
 * A hack that allows to prevent modifications to SubtypeResolver outside the initialization code.
 *
 * @since 0.21
 */
class ImmutableSubtypeResolver extends StdSubtypeResolver {

    private boolean locked;

    ImmutableSubtypeResolver(Class<?>... subtypes) {
        registerSubtypes(subtypes);

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
