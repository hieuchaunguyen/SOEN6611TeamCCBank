package cbank;

import cbank.account.Account;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SOEN 6611 - Deliverable 2 - Problem 4(b)
 * CBank V2.0 BankSystem integration tests (JUnit 5).
 *
 * Purpose: verify authentication, sessions, deposits, withdrawals,
 * currency handling, PIN changes, cash-vault rules and admin access.
 *
 * Test isolation: BankSystem is a singleton with mutable state. Before
 * each test, its private static instance is cleared by reflection so the
 * next getInstance() loads fresh database accounts and a CAD 5,000 vault.
 * This reflection is confined to test code; production code is unchanged.
 * Tests are run sequentially to avoid interference with this reset.
 *
 * Each test asserts expected behavior and logs expected/actual evidence.
 * A TestWatcher writes PASS/FAIL only after JUnit decides the outcome.
 */
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(BankSystemTest.ResultReporter.class)
class BankSystemTest {
    private static final double EPS = 0.001;
    private static final Path REPORT = Path.of("Tools", "TestResults", "bank_system_junit_results.txt");
    private BankSystem bank;

    /**
     * Create the evidence report before any tests execute.
     */
    @BeforeAll
    static void initializeReport() throws IOException {
        Files.createDirectories(REPORT.getParent());
        Files.writeString(REPORT,
                "CBank V2.0 | BankSystem JUnit 5 | D2 Problem 4(b)\n" +
                        "Fresh singleton for each test; results determined by JUnit.\n\n",
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Restore a clean BankSystem, including database, vault and session.
     */
    @BeforeEach
    void resetBank() throws Exception {
        Field singleton = BankSystem.class.getDeclaredField("instance");
        singleton.setAccessible(true);
        singleton.set(null, null);
        bank = BankSystem.getInstance();
    }

    /**
     * Write a test's expected and actual observation to console and file.
     */
    private static synchronized void evidence(String id, String label, Object expected, Object actual) {
        String line = String.format(Locale.US, "%s | %s | Expected: %s | Actual: %s%n",
                id, label, expected, actual);
        System.out.print(line);
        try {
            Files.writeString(REPORT, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save test evidence", e);
        }
    }

    /**
     * Verify a numeric result and print its observed value.
     */
    private static void balance(String id, String label, double expected, double actual) {
        evidence(id, label, String.format(Locale.US, "CAD %.2f", expected),
                String.format(Locale.US, "CAD %.2f", actual));
        assertEquals(expected, actual, EPS);
    }

    /**
     * Verify the exception type and record the observed exception.
     */
    private static <T extends Throwable> T exception(String id, Class<T> type, ExecutableAction action) {
        T caught = assertThrows(type, action::run);
        evidence(id, "Exception", type.getSimpleName(), caught.getClass().getSimpleName());
        return caught;
    }

    @FunctionalInterface
    private interface ExecutableAction {
        void run() throws Throwable;
    }

    /**
     * Record the JUnit outcome, rather than assuming a printed value means PASS.
     */
    static class ResultReporter implements TestWatcher {
        private void write(String result, ExtensionContext context) {
            String line = "RESULT: " + result + " | " + context.getRequiredTestMethod().getName() + "\n\n";
            System.out.print(line);
            try {
                Files.writeString(REPORT, line, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Could not save JUnit result: " + e.getMessage());
            }
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            write("PASS", context);
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            write("FAIL: " + cause, context);
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            write("ABORTED: " + cause, context);
        }
    }

    /**
     * TC-B01: Valid credentials authenticate the initialized commercial account.
     */
    @Test
    void test01ValidLogin() throws Exception {
        Account user = bank.login("1001", "1234");
        evidence("TC-B01", "Account number", "1001", user.getAccountNumber());
        assertEquals("1001", user.getAccountNumber());
        assertSame(user, bank.getCurrentSessionUser());
    }

    /**
     * TC-B02: Incorrect PIN must be rejected without starting a session.
     */
    @Test
    void test02WrongPin() {
        exception("TC-B02", CustomExceptions.AuthenticationException.class,
                () -> bank.login("1001", "9999"));
        evidence("TC-B02", "Session", "null", bank.getCurrentSessionUser());
        assertNull(bank.getCurrentSessionUser());
    }

    /**
     * TC-B03: Unknown account number must not authenticate.
     */
    @Test
    void test03UnknownAccount() {
        exception("TC-B03", CustomExceptions.AuthenticationException.class,
                () -> bank.login("0000", "1234"));
        assertNull(bank.getCurrentSessionUser());
    }

    /** TC-B04: Logging out clears the active session. */
    @Test void test04Logout() throws Exception {
        bank.login("1001", "1234");
        bank.logout();
        evidence("TC-B04", "Session after logout", "null", bank.getCurrentSessionUser());
        assertNull(bank.getCurrentSessionUser());
    }

    /** TC-B05: A valid withdrawal reduces account and physical vault cash. */
    @Test void test05ValidWithdrawal() throws Exception {
        bank.login("1001", "1234");
        bank.withdrawCash(200);
        balance("TC-B05", "Account balance", 1300, bank.getCurrentSessionUser().getBalanceCad());
        balance("TC-B05", "Vault balance", 4800, bank.getMachineCashVaultCad());
    }

    /** TC-B06: Insufficient account funds must not change either balance. */
    @Test void test06InsufficientFunds() throws Exception {
        bank.login("1001", "1234");
        exception("TC-B06", CustomExceptions.InsufficientFundsException.class,
                () -> bank.withdrawCash(2000));
        balance("TC-B06", "Account balance", 1500, bank.getCurrentSessionUser().getBalanceCad());
        balance("TC-B06", "Vault balance", 5000, bank.getMachineCashVaultCad());
    }

    /** TC-B07: A withdrawal above vault cash must be rejected even for an admin with funds. */
    @Test void test07VaultDepleted() throws Exception {
        bank.login("1001", "1234");
        bank.depositCash(10000, "CAD"); // Commercial account permits this deposit; vault becomes 15000.
        exception("TC-B07", CustomExceptions.VaultDepletedException.class,
                () -> bank.withdrawCash(15000.01));
        balance("TC-B07", "Account balance", 11500, bank.getCurrentSessionUser().getBalanceCad());
        balance("TC-B07", "Vault balance", 15000, bank.getMachineCashVaultCad());
    }

    /** TC-B08: CAD cash deposit increases both customer balance and physical vault. */
    @Test void test08CadDeposit() throws Exception {
        bank.login("1001", "1234");
        bank.depositCash(100, "CAD");
        balance("TC-B08", "Account balance", 1600, bank.getCurrentSessionUser().getBalanceCad());
        balance("TC-B08", "Vault balance", 5100, bank.getMachineCashVaultCad());
    }

    /** TC-B09: EUR is converted using the stored 1.57 rate, without adding CAD cash to vault. */
    @Test void test09EuroDeposit() throws Exception {
        bank.login("1001", "1234");
        bank.depositCash(100, "EUR");
        balance("TC-B09", "Account balance", 1657, bank.getCurrentSessionUser().getBalanceCad());
        balance("TC-B09", "Vault balance", 5000, bank.getMachineCashVaultCad());
    }

    /** TC-B10: An unsupported currency must be rejected with unchanged balances. */
    @Test void test10UnsupportedCurrency() throws Exception {
        bank.login("1001", "1234");
        exception("TC-B10", IllegalArgumentException.class,
                () -> bank.depositCash(100, "XYZ"));
        balance("TC-B10", "Account balance", 1500, bank.getCurrentSessionUser().getBalanceCad());
        balance("TC-B10", "Vault balance", 5000, bank.getMachineCashVaultCad());
    }

    /** TC-B11: An invalid PIN length is rejected and the old PIN remains valid. */
    @Test void test11InvalidPinChange() throws Exception {
        bank.login("1001", "1234");
        exception("TC-B11", CustomExceptions.InvalidPinException.class,
                () -> bank.changePin("123"));
        evidence("TC-B11", "Old PIN valid", true, bank.getCurrentSessionUser().validatePin("1234"));
        assertTrue(bank.getCurrentSessionUser().validatePin("1234"));
    }

    /** TC-B12: A valid four-digit PIN replaces the old one. */
    @Test void test12ValidPinChange() throws Exception {
        bank.login("1001", "1234");
        bank.changePin("4321");
        Account user = bank.getCurrentSessionUser();
        evidence("TC-B12", "New PIN valid", true, user.validatePin("4321"));
        evidence("TC-B12", "Old PIN valid", false, user.validatePin("1234"));
        assertTrue(user.validatePin("4321"));
        assertFalse(user.validatePin("1234"));
        bank.logout();
        assertSame(user, bank.login("1001", "4321"));
    }

    /** TC-B13: A normal customer cannot retrieve administrative audit logs. */
    @Test void test13CustomerCannotReadAuditLogs() throws Exception {
        bank.login("1001", "1234");
        exception("TC-B13", SecurityException.class, bank::getAuditLogsFormatted);
    }

    /** TC-B14: A normal customer cannot replenish physical vault cash. */
    @Test void test14CustomerCannotReplenishVault() throws Exception {
        bank.login("1001", "1234");
        exception("TC-B14", SecurityException.class,
                () -> bank.adminReplenishVault(500));
        balance("TC-B14", "Vault balance", 5000, bank.getMachineCashVaultCad());
    }

    /** TC-B15: Admin may replenish vault cash by a valid positive amount. */
    @Test void test15AdminReplenishesVault() throws Exception {
        bank.login("9999", "0000");
        bank.adminReplenishVault(500);
        balance("TC-B15", "Vault balance", 5500, bank.getMachineCashVaultCad());
    }

    /** TC-B16: Admin audit logs include successful login and withdrawal records. */
    @Test void test16AdminReadsAuditLogs() throws Exception {
        bank.login("1001", "1234");
        bank.withdrawCash(100);
        bank.logout();
        bank.login("9999", "0000");
        String logs = bank.getAuditLogsFormatted();
        evidence("TC-B16", "Audit contains LOGIN", true, logs.contains("LOGIN"));
        evidence("TC-B16", "Audit contains WITHDRAWAL", true, logs.contains("WITHDRAWAL"));
        assertTrue(logs.contains("LOGIN"));
        assertTrue(logs.contains("WITHDRAWAL"));
    }
}
