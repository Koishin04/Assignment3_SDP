package rume;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CandidateSourceTest {
    private List<CandidateSource> sources() {
        return List.of(new RuMeCandidateSource(), new UniversityCandidateSource(),
                new LegacyCandidateSourceAdapter());
    }

    @Test
    void sourcesSupportCaseInsensitiveTrimmedCities() throws Exception {
        for (CandidateSource source : sources()) {
            List<Candidate> result = source.findByCity(" ASTANA ");
            assertFalse(result.isEmpty());
            for (Candidate candidate : result) {
                assertEquals("Astana", candidate.getCity());
            }
        }
    }

    @Test
    void unknownCityReturnsEmptyList() throws Exception {
        for (CandidateSource source : sources()) {
            assertTrue(source.findByCity("London").isEmpty());
        }
    }

    @Test
    void nullAndBlankCityUseCommonException() {
        for (CandidateSource source : sources()) {
            assertThrows(CandidateSourceException.class, () -> source.findByCity(null));
            assertThrows(CandidateSourceException.class, () -> source.findByCity(" "));
        }
    }

    @Test
    void modifyingReturnedListDoesNotChangeCatalog() throws Exception {
        for (CandidateSource source : sources()) {
            int size = source.findByCity("Astana").size();
            source.findByCity("Astana").clear();
            assertEquals(size, source.findByCity("Astana").size());
        }
    }

    @Test
    void candidatePreservesValidFieldsAndReadableOutput() {
        Candidate candidate = new Candidate(1, " Arman ", " Astana ", 80000);
        assertEquals(1, candidate.getId());
        assertEquals("Arman", candidate.getName());
        assertEquals("Astana", candidate.getCity());
        assertEquals(80000, candidate.getBudget());
        assertEquals("1 | Arman | Astana | 80000 KZT/month", candidate.toString());
    }

    @Test
    void candidateRejectsInvalidFields() {
        assertThrows(IllegalArgumentException.class, () -> new Candidate(0, "One", "Astana", 1));
        assertThrows(IllegalArgumentException.class, () -> new Candidate(1, " ", "Astana", 1));
        assertThrows(IllegalArgumentException.class, () -> new Candidate(1, "One", null, 1));
        assertThrows(IllegalArgumentException.class, () -> new Candidate(1, "One", "Astana", 0));
    }
}
