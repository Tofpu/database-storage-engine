package io.tofpu.databasestorage.resolver.key.impl;

import io.tofpu.databasestorage.resolver.key.StorageKeyResolver;

public class StorageNumberSerializer extends StorageKeyResolver<Number> {
    public StorageNumberSerializer() {
        super(Number.class);
    }

    @Override
    public String serialize(final Object obj) {
        return obj.toString();
    }

    @Override
    public Number deserialize(final String serialized) {
        return Number.class.cast(serialized);
    }
}
