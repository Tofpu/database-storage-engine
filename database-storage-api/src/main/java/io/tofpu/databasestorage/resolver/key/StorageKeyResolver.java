package io.tofpu.databasestorage.resolver.key;

public abstract class StorageKeyResolver<T> {
    private final Class<T> type;

    public StorageKeyResolver(final Class<T> type) {
        this.type = type;
    }

    public abstract String serialize(final Object obj);
    public abstract T deserialize(final String serialized);

    public Class<T> getType() {
        return type;
    }
}
