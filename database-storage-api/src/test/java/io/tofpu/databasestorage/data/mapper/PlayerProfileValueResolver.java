package io.tofpu.databasestorage.data.mapper;

import io.tofpu.databasestorage.StorageBase;
import io.tofpu.databasestorage.data.PlayerProfile;
import io.tofpu.databasestorage.resolver.value.StorageValueResolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerProfileValueResolver extends StorageValueResolver<UUID, PlayerProfile> {
    private static final String INSET_PROFILE_QUERY =
            "INSERT OR REPLACE INTO " + "player_profile" + " (id," +
            "name) VALUES (?, ?)";
    private static final String SELECT_PROFILE_QUERY = "SELECT * FROM player_profile WHERE id = ?";

    public PlayerProfileValueResolver(final StorageBase storageBase) {
        super(PlayerProfile.class, storageBase);
    }

    @Override
    public void save(final String key, Object value) throws SQLException {
        final Connection connection = storageBase.getConnection();

        final PlayerProfile playerProfile = (PlayerProfile) value;

        try (final PreparedStatement statement = connection.prepareStatement(INSET_PROFILE_QUERY)) {
            statement.setString(1, key);
            statement.setObject(2, playerProfile.getName());

            statement.executeUpdate();
        }
    }

    @Override
    public PlayerProfile retrieve(final String key) {
        final Connection connection = storageBase.getConnection();

        try (final PreparedStatement statement = connection.prepareStatement(SELECT_PROFILE_QUERY)) {
            System.out.println("key: " + key + " type: " + key.getClass());
            statement.setString(1, key);

            try (final ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.isClosed()) {
                    System.out.println("ResultSet is closed");
                    return null;
                }

                return new PlayerProfile(UUID.fromString(resultSet.getString("id")), resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public PlayerProfile delete(final String key) {
        return null;
    }
}
