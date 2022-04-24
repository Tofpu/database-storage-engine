package io.tofpu.databasestorage;

import io.tofpu.databasestorage.resolver.value.StorageValueResolver;
import io.tofpu.databasestorage.resolver.key.StorageKeyResolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SQLiteStorage extends StorageBase {
    private Connection connection;

    @Override
    CompletableFuture<Boolean> init() {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            future.completeExceptionally(e);
            throw new IllegalStateException(e);
        }

        CompletableFuture.runAsync(() -> {
            establishConnection();
            execute("CREATE TABLE IF NOT EXISTS player_profile (id STRING PRIMARY KEY, " +
                    "name" + " STRING NOT NULL)", statement -> {
                try {
                    statement.execute();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            });

            future.complete(true);
        });
        return future;
    }

    private void establishConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:test.db");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void execute(final String sql,
            final Consumer<PreparedStatement> statementConsumer) {
        try (final PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statementConsumer.accept(statement);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    List<StorageValueResolver<?, ?>> getValueResolvers() {
        return Arrays.asList(new StorageBaseTest.PlayerProfileMapper(this));
    }

    @Override
    List<StorageKeyResolver<?>> getKeyResolvers() {
        return Collections.emptyList();
    }

    @Override
    Connection getConnection() {
        if (connection == null) {
            establishConnection();
        }
        return connection;
    }
}
