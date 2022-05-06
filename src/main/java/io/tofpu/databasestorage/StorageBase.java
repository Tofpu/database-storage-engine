package io.tofpu.databasestorage;

import io.tofpu.databasestorage.resolver.value.StorageValueResolver;
import io.tofpu.databasestorage.resolver.key.StorageKeyResolver;
import io.tofpu.databasestorage.resolver.key.impl.StorageStringResolver;
import io.tofpu.databasestorage.resolver.key.impl.StorageUUIDResolver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An abstract class that provides the basic functionality for a database storage.
 */
public abstract class StorageBase {
    private final Map<Class<?>, StorageValueResolver<?, ?>> valueResolverMap = new HashMap<>();
    private final Map<Class<?>, StorageKeyResolver<?>> keyResolverMap = new HashMap<>();

    private final ExecutorService executorService = getExecutorService();

    protected StorageBase() {
        for (final StorageValueResolver<?, ?> mapper : getValueResolvers()) {
            valueResolverMap.put(mapper.getType(), mapper);
        }

        for (final StorageKeyResolver<?> serializer : getKeyResolvers()) {
            keyResolverMap.put(serializer.getType(), serializer);
        }

        keyResolverMap.put(String.class, new StorageStringResolver());
        keyResolverMap.put(UUID.class, new StorageUUIDResolver());
    }

    /**
     * Initializes the database connection asynchronously.
     *
     * @return true if the connection was established, false otherwise
     */
    abstract CompletableFuture<Boolean> init();

    /**
     * You can specify a list of {@link StorageValueResolver}s to be used by the
     * {@link StorageBase}.
     *
     * @return a list of {@link StorageValueResolver}s
     */
    abstract List<StorageValueResolver<?, ?>> getValueResolvers();

    /**
     * You can specify a list of {@link StorageKeyResolver}s to be used by the
     * {@link StorageBase}.
     *
     * @return a list of {@link StorageKeyResolver}s
     */
    abstract List<StorageKeyResolver<?>> getKeyResolvers();

    /**
     * This method should establish a connection to the database when called.
     */
    protected abstract void establishConnection();

    /**
     * @return If the database connection is null, this method will attempt
     * to establish a connection via {@link #establishConnection()} method, otherwise it
     * will return the current connection
     */
    public abstract Connection getConnection();

    /**
     * This method will attempt to save the value to the database by
     * finding the appropriate {@link StorageValueResolver}, and then
     * calling the {@link StorageValueResolver#save(String, Object)} method
     * asynchronously.
     *
     * @param key the key to be passed to the {@link StorageValueResolver}
     * @param value the value to be passed to the {@link StorageValueResolver}
     *
     * @return true if the value was successfully stored, false otherwise
     * @throws IllegalStateException if no {@link StorageKeyResolver} is found for
     * the given type
     * @throws IllegalStateException if no {@link StorageValueResolver} is found for
     * the given type
     */
    public CompletableFuture<Boolean> saveAsync(final Object key, final Object value) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();

        final StorageKeyResolver<?> keyResolver = getKeyResolver(key.getClass());
        if (keyResolver == null) {
            throw new IllegalArgumentException("No keyResolver found for key type " + key.getClass());
        }

        final StorageValueResolver<?, ?> valueResolver = getValueResolver(value.getClass());
        if (valueResolver == null) {
            throw new IllegalArgumentException("No valueResolver found for value type " + value.getClass());
        }

        CompletableFuture.runAsync(() -> {
            try {
                valueResolver.save(keyResolver.serialize(key), value);
                future.complete(true);
            } catch (SQLException e) {
                future.completeExceptionally(e);
                throw new IllegalStateException(e);
            }
        }, executorService);

        return future;
    }

    /**
     * This method will attempt to load the value from the database by
     * finding the appropriate {@link StorageValueResolver}, and then
     * calling the {@link StorageValueResolver#retrieve(String)} method
     * asynchronously.
     *
     * @param key the key to be passed to the {@link StorageValueResolver}
     * @param valueType the type of the value to be passed to the {@link StorageValueResolver}
     * @param <T> the type of the value
     *
     * @return the value stored in the database, or null if no value was found
     * @throws IllegalStateException if no {@link StorageKeyResolver} is found for
     * the given type
     * @throws IllegalStateException if no {@link StorageValueResolver} is found for
     * the given type
     */
    public <T> CompletableFuture<T> retrieveAsync(final Object key,
            final Class<T> valueType) {
        final StorageKeyResolver<?> keyResolver = getKeyResolver(key.getClass());
        if (keyResolver == null) {
            throw new IllegalArgumentException("No keyResolver found for key type " + key.getClass());
        }

        final StorageValueResolver<?, ?> valueResolver = getValueResolver(valueType);
        if (valueResolver == null) {
            throw new IllegalArgumentException("No valueResolver found for value type " + valueType);
        }

        return (CompletableFuture<T>) CompletableFuture.supplyAsync(() -> {
            try {
                return valueResolver.retrieve(keyResolver.serialize(key));
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }, executorService);
    }

    /**
     * This method will attempt to delete the value from the database by
     * finding the appropriate {@link StorageValueResolver}, and then
     * calling the {@link StorageValueResolver#delete(String)} method
     * asynchronously.
     *
     * @param key the key to be passed to the {@link StorageValueResolver}
     * @param valueType the type of the value
     *
     * @return true if the value was successfully deleted, false otherwise
     */
    public CompletableFuture<Boolean> deleteAsync(final Object key, final Class<?> valueType) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();

        final StorageKeyResolver<?> keyResolver = getKeyResolver(key.getClass());
        if (keyResolver == null) {
            throw new IllegalArgumentException("No keyResolver found for key type " + key.getClass());
        }

        final StorageValueResolver<?, ?> valueResolver = getValueResolver(valueType);
        if (valueResolver == null) {
            throw new IllegalArgumentException("No valueResolver found for value type " + valueType);
        }

        CompletableFuture.runAsync(() -> {
            try {
                valueResolver.delete(keyResolver.serialize(key));
            } catch (SQLException e) {
                future.completeExceptionally(e);
                throw new IllegalStateException(e);
            }
            future.complete(true);
        }, executorService);

        return future;
    }

    /**
     * This method shall be overridden by subclasses if you wish to
     * return your own {@link ExecutorService} for asynchronous
     * operations.
     *
     * @return the executorService to be used for asynchronous operations
     */
    public ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(2);
    }

    /**
     * This method will attempt to retrieve the {@link StorageValueResolver}
     * for the given type.
     *
     * @param type the type of the value
     * @param <T> the type of the value
     *
     * @return the {@link StorageValueResolver} if found, null otherwise
     */
    public <T> StorageValueResolver<?, T> getValueResolver(final Class<T> type) {
        return (StorageValueResolver<?, T>) valueResolverMap.get(type);
    }

    /**
     * This method will attempt to retrieve the {@link StorageKeyResolver}
     * for the given type.
     *
     * @param type the type of the key
     * @param <T> the type of the key
     *
     * @return the {@link StorageKeyResolver} if found, null otherwise
     */
    public <T> StorageKeyResolver<T> getKeyResolver(final Class<T> type) {
        return (StorageKeyResolver<T>) keyResolverMap.get(type);
    }

    /**
     * This method will close the connection from the database,
     * and then shutdown the {@link ExecutorService} threads.
     */
    public void shutdown() {
        final Connection connection = getConnection();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }

        executorService.shutdown();
    }
}
