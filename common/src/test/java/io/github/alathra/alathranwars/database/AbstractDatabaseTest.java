package io.github.alathra.alathranwars.database;

import io.github.alathra.alathranwars.database.config.DatabaseConfig;
import io.github.alathra.alathranwars.database.exception.DatabaseInitializationException;
import io.github.alathra.alathranwars.utility.DB;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains all test cases.
 */
@Tag("database")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class AbstractDatabaseTest {
    private final DatabaseTestParams testConfig;
    public DatabaseConfig databaseConfig;
    public Logger logger = LoggerFactory.getLogger("Database");

    AbstractDatabaseTest(DatabaseTestParams testConfig) {
        this.testConfig = testConfig;
    }

    /**
     * Exposes the database parameters of this test.
     *
     * @return the database test config
     */
    public DatabaseTestParams getTestConfig() {
        return testConfig;
    }

    @BeforeEach
    void beforeEachTest() {
    }

    @AfterEach
    void afterEachTest() {
    }

    @AfterAll
    void afterAllTests() {
        DB.getHandler().doShutdown(); // Shut down the connection pool after all tests have been run
    }

    @Test
    @Order(1) // This forces migrations to be run before any other queries are tested
    @DisplayName("Flyway migrations")
    void testMigrations() throws DatabaseInitializationException {
        DB.getHandler().migrate();
    }

    @Test
    @Order(2)
    @DisplayName("Schema contains the base tables")
    void testBaseTablesExist() {
        assertTablesExist("messaging", "cooldowns");
    }

    @Test
    @Order(3)
    @DisplayName("Schema contains the war tables")
    void testWarTablesExist() {
        assertTablesExist("list", "sides", "sides_nations", "sides_players", "sides_towns", "sides_spawns", "sieges", "siege_players");
    }

    /**
     * Asserts every named table exists, applying the prefix this parameterisation runs with.
     *
     * <p>Table name comparison is case insensitive on purpose. H2 folds unquoted identifiers to
     * upper case while SQLite and MySQL do not, and the migrations quote them, so the stored case
     * differs by vendor.
     */
    private void assertTablesExist(String... names) {
        final String prefix = getTestConfig().tablePrefix();

        try (Connection con = DB.getConnection()) {
            final DSLContext context = DB.getContext(con);
            final Set<String> actual = context.meta().getTables().stream()
                .map(t -> t.getName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

            for (String name : names) {
                final String expected = (prefix + name).toLowerCase(Locale.ROOT);
                Assertions.assertTrue(
                    actual.contains(expected),
                    () -> "Expected table \"" + expected + "\" after migration, found: " + actual
                );
            }
        } catch (SQLException e) {
            Assertions.fail("Failed to read schema metadata", e);
        }
    }
}
