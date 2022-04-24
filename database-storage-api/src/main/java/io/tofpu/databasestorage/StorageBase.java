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

public abstract class StorageBase {
    private final Map<Class<?>, StorageValueResolver<?, ?>> valueResolverMap = new HashMap<>();
    private final Map<Class<?>, StorageKeyResolver<?>> keyResolverMap = new HashMap<>();

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

    abstract CompletableFuture<Boolean> init();

    abstract List<StorageValueResolver<?, ?>> getValueResolvers();
    abstract List<StorageKeyResolver<?>> getKeyResolvers();

    abstract Connection getConnection();

    public CompletableFuture<Boolean> save(final Object key, final Object value) {
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
        });

        return future;
    }

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

        return (CompletableFuture<T>) CompletableFuture.supplyAsync(() -> valueResolver.retrieve(keyResolver.serialize(key)));
    }

    public CompletableFuture<Boolean> delete(final Object key, final Class<?> valueType) {
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
            valueResolver.delete(keyResolver.serialize(key));
            future.complete(true);
        });

        return future;
    }

    public void shutdown() {
        final Connection connection = getConnection();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public <T> StorageValueResolver<?, T> getValueResolver(final Class<T> type) {
        return (StorageValueResolver<?, T>) valueResolverMap.get(type);
    }

    public <T> StorageKeyResolver<T> getKeyResolver(final Class<T> type) {
        return (StorageKeyResolver<T>) keyResolverMap.get(type);
    }
}
