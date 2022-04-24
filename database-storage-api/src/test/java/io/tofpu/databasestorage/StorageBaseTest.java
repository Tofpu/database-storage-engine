package io.tofpu.databasestorage;

import io.tofpu.databasestorage.data.PlayerProfile;
import io.tofpu.databasestorage.resolver.value.StorageValueResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageBaseTest {
    private static StorageBase storageBase;
    private static PlayerProfile playerProfile;

    @BeforeAll
    public static void setUp() {
        storageBase = new SQLiteStorage();
        playerProfile = new PlayerProfile(UUID.randomUUID(), "Tofpu");
    }

    @Test
    @Order(1)
    public void connection_should_be_established() {
        storageBase.init();

        assert (storageBase.getConnection() != null);
    }

    @Test
    @Order(2)
    public void profile_should_be_inserted() throws ExecutionException, InterruptedException {
        assertTrue(storageBase.save(playerProfile.getUUID(), playerProfile)
                .get(), "Profile was not saved");
    }

    @Test
    @Order(3)
    public void profile_should_be_received() throws ExecutionException, InterruptedException {
        final PlayerProfile retrievedProfile = storageBase.retrieveAsync(playerProfile.getUUID(), PlayerProfile.class)
                .get();

        assertNotNull(retrievedProfile, "Retrieved Player Profile is null");
        assertEquals(retrievedProfile, playerProfile,
                "Retrieved profile is not equal " + "to static profile");
    }

    @Test
    @Order(4)
    public void profile_should_be_deleted() throws ExecutionException, InterruptedException {
        assertTrue(storageBase.delete(playerProfile.getUUID(), PlayerProfile.class)
                .get(), "Profile was not deleted");

        assertNotNull(storageBase.retrieveAsync(playerProfile.getUUID(), PlayerProfile.class)
                .get(), "Profile was not deleted");
    }

    @AfterAll
    public static void tearDown() {
        final File databaseFile = new File("test.db");
        if (databaseFile.exists()) {
            databaseFile.delete();
        }

        storageBase.shutdown();
    }

    public static class PlayerProfileMapper extends StorageValueResolver<UUID, PlayerProfile> {
        private static final String INSET_PROFILE_QUERY =
                "INSERT OR REPLACE INTO " + "player_profile" + " (id," +
                "name) VALUES (?, ?)";
        private static final String SELECT_PROFILE_QUERY =
                "SELECT * FROM player_profile WHERE id = ?";

        protected PlayerProfileMapper(final StorageBase storageBase) {
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
}
