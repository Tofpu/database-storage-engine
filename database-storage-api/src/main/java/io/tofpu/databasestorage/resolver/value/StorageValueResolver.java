package io.tofpu.databasestorage.resolver.value;

import io.tofpu.databasestorage.StorageBase;

import java.sql.SQLException;

public abstract class StorageValueResolver<K, V> {
    private final Class<V> type;
    protected final StorageBase storageBase;

    protected StorageValueResolver(final Class<V> type, final StorageBase storageBase) {
        this.type = type;
        this.storageBase = storageBase;
    }

    public abstract void save(final String key, final Object value) throws SQLException;
    public abstract V retrieve(final String key);
    public abstract V delete(final String key);

    public Class<V> getType() {
        return type;
    }
}
