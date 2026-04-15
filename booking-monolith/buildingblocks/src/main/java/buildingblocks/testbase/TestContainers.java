package buildingblocks.testbase;

import org.testcontainers.containers.PostgreSQLContainer;

public final class TestContainers {
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withExposedPorts(5432);

    private static boolean initialized = false;

    public static synchronized void initializeContainersOnce() {
        if (!initialized) {
            postgres.start();
            initialized = true;

            Runtime.getRuntime().addShutdownHook(new Thread(TestContainers::stopContainers));
        }
    }

    public static synchronized void stopContainers() {
        postgres.stop();
    }
}
