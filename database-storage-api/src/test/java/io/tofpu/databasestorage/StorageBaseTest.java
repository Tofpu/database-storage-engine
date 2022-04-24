package io.tofpu.databasestorage;

import io.tofpu.databasestorage.data.PlayerProfile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertTrue(storageBase.saveAsync(playerProfile.getUUID(), playerProfile)
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
        assertTrue(storageBase.deleteAsync(playerProfile.getUUID(), PlayerProfile.class)
                .get(), "Profile was not deleted");

        assertNull(storageBase.retrieveAsync(playerProfile.getUUID(), PlayerProfile.class)
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
}
