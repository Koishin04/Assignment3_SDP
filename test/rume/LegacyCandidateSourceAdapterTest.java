package rume;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LegacyCandidateSourceAdapterTest {
    private static class StubLegacyService extends LegacyHousingService {
        LegacyResult response;
        RuntimeException failure;
        int receivedCode;
        int calls;

        StubLegacyService(LegacyResult response) {
            this.response = response;
        }

        @Override
        public LegacyResult loadProfiles(int cityCode) {
            receivedCode = cityCode;
            calls++;
            if (failure != null) {
                throw failure;
            }
            return response;
        }
    }

    private void assertFailure(LegacyResult response, String message) {
        StubLegacyService service = new StubLegacyService(response);
        CandidateSource adapter = new LegacyCandidateSourceAdapter(service);
        CandidateSourceException error = assertThrows(CandidateSourceException.class,
                () -> adapter.findByCity("Astana"));
        assertEquals(message, error.getMessage());
        assertNull(error.getCause());
        assertEquals(1, service.calls);
    }

    @Test
    void translatesCityAndStringRecords() throws Exception {
        StubLegacyService service = new StubLegacyService(new LegacyResult(0,
                new String[][]{{"501", "Test User", "1", "80000"}}));
        CandidateSource adapter = new LegacyCandidateSourceAdapter(service);
        List<Candidate> result = adapter.findByCity(" ASTANA ");
        assertEquals(1, service.receivedCode);
        assertEquals(1, service.calls);
        assertEquals(1, result.size());
        assertEquals(501, result.get(0).getId());
        assertEquals("Test User", result.get(0).getName());
        assertEquals("Astana", result.get(0).getCity());
        assertEquals(80000, result.get(0).getBudget());
    }

    @Test
    void adaptsEverySupportedCity() throws Exception {
        CandidateSource adapter = new LegacyCandidateSourceAdapter();
        assertEquals(2, adapter.findByCity("Astana").size());
        assertEquals("Almaty", adapter.findByCity("Almaty").get(0).getCity());
        assertEquals("Aktobe", adapter.findByCity("Aktobe").get(0).getCity());
    }

    @Test
    void translatesInvalidRequestStatus() {
        assertFailure(new LegacyResult(LegacyHousingService.INVALID_REQUEST, null),
                "Candidate source rejected the request.");
    }

    @Test
    void translatesUnavailableStatus() {
        assertFailure(new LegacyResult(LegacyHousingService.UNAVAILABLE, null),
                "Candidate source is unavailable.");
    }

    @Test
    void translatesFailureFromRealUnavailableService() {
        CandidateSource source = new LegacyCandidateSourceAdapter(new LegacyHousingService(false));
        CandidateSourceException error = assertThrows(CandidateSourceException.class,
                () -> source.findByCity("Astana"));
        assertEquals("Candidate source is unavailable.", error.getMessage());
    }

    @Test
    void translatesUnknownStatus() {
        assertFailure(new LegacyResult(99, null), "Candidate source returned an unknown failure.");
    }

    @Test
    void translatesNullResponse() {
        assertFailure(null, "Candidate source returned invalid data.");
    }

    @Test
    void translatesRuntimeFailureWithoutLeakingItsMessageOrCause() {
        StubLegacyService service = new StubLegacyService(null);
        service.failure = new IllegalStateException("Private legacy failure details");
        CandidateSource adapter = new LegacyCandidateSourceAdapter(service);
        CandidateSourceException error = assertThrows(CandidateSourceException.class,
                () -> adapter.findByCity("Astana"));
        assertEquals("Candidate source failed.", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void translatesNullRecords() {
        assertFailure(new LegacyResult(0, null), "Candidate source returned invalid data.");
    }

    @Test
    void translatesNullRecord() {
        assertFailure(new LegacyResult(0, new String[][]{null}), "Candidate source returned invalid data.");
    }

    @Test
    void translatesWrongRecordLength() {
        assertFailure(new LegacyResult(0, new String[][]{{"1", "One"}}),
                "Candidate source returned invalid data.");
    }

    @Test
    void translatesInvalidNumericValues() {
        String[][] invalidRecords = {
                {"not-a-number", "One", "1", "80000"},
                {"1", "One", "invalid", "80000"},
                {"1", "One", "1", "invalid"},
                {"1", "One", "1", "999999999999999999999"}
        };
        for (String[] record : invalidRecords) {
            assertFailure(new LegacyResult(0, new String[][]{record}),
                    "Candidate source returned invalid data.");
        }
    }

    @Test
    void translatesRecordForWrongCity() {
        assertFailure(new LegacyResult(0, new String[][]{{"1", "One", "2", "80000"}}),
                "Candidate source returned invalid data.");
    }

    @Test
    void translatesInvalidCandidateFields() {
        String[][] invalidRecords = {
                {"0", "One", "1", "80000"},
                {"1", " ", "1", "80000"},
                {"1", null, "1", "80000"},
                {"1", "One", "1", "0"},
                {"1", "One", "1", "-500"}
        };
        for (String[] record : invalidRecords) {
            assertFailure(new LegacyResult(0, new String[][]{record}),
                    "Candidate source returned invalid data.");
        }
    }

    @Test
    void unsupportedCityIsEmptyWithoutCallingLegacyService() throws Exception {
        StubLegacyService service = new StubLegacyService(null);
        CandidateSource adapter = new LegacyCandidateSourceAdapter(service);
        assertTrue(adapter.findByCity("London").isEmpty());
        assertEquals(0, service.calls);
    }

    @Test
    void rejectsNullOrBlankCity() {
        CandidateSource adapter = new LegacyCandidateSourceAdapter();
        assertThrows(CandidateSourceException.class, () -> adapter.findByCity(null));
        assertThrows(CandidateSourceException.class, () -> adapter.findByCity(" "));
    }

    @Test
    void successfulEmptyResponseIsNotAnError() throws Exception {
        StubLegacyService service = new StubLegacyService(new LegacyResult(0, new String[0][]));
        CandidateSource adapter = new LegacyCandidateSourceAdapter(service);
        assertTrue(adapter.findByCity("Astana").isEmpty());
    }

    @Test
    void rejectsNullWrappedService() {
        assertThrows(IllegalArgumentException.class, () -> new LegacyCandidateSourceAdapter(null));
    }
}
