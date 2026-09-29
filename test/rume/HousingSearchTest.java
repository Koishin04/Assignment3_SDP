package rume;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HousingSearchTest {
    private static class StubSource implements CandidateSource {
        String receivedCity;
        int calls;
        CandidateSourceException failure;
        List<Candidate> candidates = List.of(
                new Candidate(1, "One", "Astana", 80000),
                new Candidate(2, "Two", "Astana", 100000),
                new Candidate(3, "Three", "Almaty", 80000),
                new Candidate(4, "Four", "Astana", 60000));

        public String getCode() {
            return "stub";
        }

        public List<Candidate> findByCity(String city) throws CandidateSourceException {
            receivedCity = city;
            calls++;
            if (failure != null) {
                throw failure;
            }
            return candidates;
        }
    }

    @Test
    void roommateDelegatesAndRequiresEqualBudget() throws Exception {
        StubSource source = new StubSource();
        HousingSearch search = new RoommateSearch();
        search.setSource(source);
        List<Candidate> result = search.search(" Astana ", 80000);
        assertEquals("Astana", source.receivedCity);
        assertEquals(1, source.calls);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void tenantDelegatesAndAcceptsEqualOrHigherBudget() throws Exception {
        StubSource source = new StubSource();
        HousingSearch search = new TenantSearch();
        search.setSource(source);
        List<Candidate> result = search.search("Astana", 80000);
        assertEquals("Astana", source.receivedCity);
        assertEquals(1, source.calls);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
    }

    @Test
    void bothSearchesPropagateTheSourceContractError() {
        for (HousingSearch search : List.of(new RoommateSearch(), new TenantSearch())) {
            StubSource source = new StubSource();
            source.failure = new CandidateSourceException("Candidate source is unavailable.");
            search.setSource(source);
            CandidateSourceException error = assertThrows(CandidateSourceException.class,
                    () -> search.search("Astana", 80000));
            assertSame(source.failure, error);
        }
    }

    @Test
    void rejectsSearchWithoutSource() {
        assertThrows(IllegalStateException.class, () -> new RoommateSearch().search("Astana", 80000));
    }

    @Test
    void rejectsNullSource() {
        assertThrows(IllegalArgumentException.class, () -> new RoommateSearch().setSource(null));
    }

    @Test
    void rejectsInvalidInputBeforeCallingSource() {
        StubSource source = new StubSource();
        HousingSearch search = new RoommateSearch();
        search.setSource(source);
        assertThrows(IllegalArgumentException.class, () -> search.search(null, 80000));
        assertThrows(IllegalArgumentException.class, () -> search.search(" ", 80000));
        assertThrows(IllegalArgumentException.class, () -> search.search("Astana", 0));
        assertThrows(IllegalArgumentException.class, () -> search.search("Astana", -1));
        assertEquals(0, source.calls);
    }

    @Test
    void returnsEmptyListWhenBudgetDoesNotMatch() throws Exception {
        HousingSearch search = new RoommateSearch();
        search.setSource(new StubSource());
        assertTrue(search.search("Astana", 85000).isEmpty());
    }

    @Test
    void comparesCitiesWithoutCaseSensitivity() throws Exception {
        HousingSearch search = new RoommateSearch();
        search.setSource(new StubSource());
        assertEquals(1, search.search("ASTANA", 80000).size());
    }

    @Test
    void bothSearchesWorkWithAllThreeSources() throws Exception {
        List<CandidateSource> sources = List.of(new RuMeCandidateSource(),
                new UniversityCandidateSource(), new LegacyCandidateSourceAdapter());
        for (CandidateSource source : sources) {
            HousingSearch roommate = new RoommateSearch();
            roommate.setSource(source);
            assertEquals(1, roommate.search("Astana", 80000).size());
            HousingSearch tenant = new TenantSearch();
            tenant.setSource(source);
            assertEquals(2, tenant.search("Astana", 80000).size());
        }
    }

    @Test
    void sameAbstractionCanSwitchSourceAtRuntime() throws Exception {
        HousingSearch search = new RoommateSearch();
        search.setSource(new RuMeCandidateSource());
        assertEquals(1, search.search("Astana", 80000).get(0).getId());
        search.setSource(new LegacyCandidateSourceAdapter());
        assertEquals(201, search.search("Astana", 80000).get(0).getId());
    }
}
