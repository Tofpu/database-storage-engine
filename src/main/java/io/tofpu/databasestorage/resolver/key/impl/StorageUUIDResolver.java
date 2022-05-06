package io.tofpu.databasestorage.resolver.key.impl;

import io.tofpu.databasestorage.resolver.key.StorageKeyResolver;

import java.util.UUID;

public class StorageUUIDResolver extends StorageKeyResolver<UUID> {
    public StorageUUIDResolver() {
        super(UUID.class);
    }

    @Override
    public String serialize(final Object obj) {
        return obj.toString();
    }
}
