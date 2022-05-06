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

    /**
     * This method will be called asynchronously by
     * the {@link StorageBase} class.
     *
     * @param key the value key
     * @param value the value to store in the database
     *
     * @throws SQLException if an error occurs while storing the value
     */
    public abstract void save(final String key, final Object value) throws SQLException;

    /**
     * @param key the key that is used to retrieve the value
     * from the database
     *
     * @return the value that is stored in the database, or null otherwise.
     * @throws SQLException if an error occurs while retrieving the value
     */
    public abstract V retrieve(final String key) throws SQLException;

    /**
     * This method will be called asynchronously by
     * the {@link StorageBase} class.
     *
     * @param key the key that is associated with a value
     * @throws SQLException if an error occurs while deleting the value
     */
    public abstract void delete(final String key) throws SQLException;

    /**
     * @return the value resolver type
     */
    public Class<V> getType() {
        return type;
    }
}
