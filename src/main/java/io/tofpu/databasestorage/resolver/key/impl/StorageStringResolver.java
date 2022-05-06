package io.tofpu.databasestorage.resolver.key.impl;

import io.tofpu.databasestorage.resolver.key.StorageKeyResolver;

public class StorageStringResolver extends StorageKeyResolver<String> {
    public StorageStringResolver() {
        super(String.class);
    }

    @Override
    public String serialize(final Object obj) {
        return obj.toString();
    }

    @Override
    public String deserialize(final String serialized) {
        return serialized;
    }
}
