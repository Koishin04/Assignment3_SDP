package rume;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppRegistryTest {
    private static class ExtraSource implements CandidateSource {
        public String getCode() {
            return "extra";
        }

        public List<Candidate> findByCity(String city) {
            return List.of(new Candidate(901, "New User", "Astana", 90000));
        }
    }

    private static class ExtraSearch extends HousingSearch {
        public String getCode() {
            return "3";
        }

        public String getDescription() {
            return "Test-only extra search";
        }

        public String getBudgetPrompt() {
            return "Enter budget: ";
        }

        protected boolean matchesBudget(int candidateBudget, int requestedBudget) {
            return candidateBudget > requestedBudget;
        }
    }

    @Test
    void loadsTwoSearchesAndExactlyThreeProductionSources() {
        AppRegistry registry = new AppRegistry();
        registry.load();
        assertEquals(2, registry.getSearches().size());
        assertEquals(List.of("rume", "university", "legacy"), registry.getSourceCodes());
    }

    @Test
    void selectsAdaptedSourceFromInput() throws Exception {
        AppRegistry registry = new AppRegistry();
        registry.load();
        HousingSearch search = registry.getSearch(" 1 ");
        search.setSource(registry.getSource(" LEGACY "));
        assertEquals(201, search.search("Astana", 80000).get(0).getId());
    }

    @Test
    void rejectsUnknownAndMissingCodes() {
        AppRegistry registry = new AppRegistry();
        registry.load();
        assertThrows(IllegalArgumentException.class, () -> registry.getSource("missing"));
        assertThrows(IllegalArgumentException.class, () -> registry.getSearch("99"));
        assertThrows(IllegalArgumentException.class, () -> registry.getSource(null));
        assertThrows(IllegalArgumentException.class, () -> registry.getSearch(" "));
    }

    @Test
    void rejectsDuplicateRegistrations() {
        AppRegistry registry = new AppRegistry();
        registry.registerSource(new RuMeCandidateSource());
        registry.registerSearch(new RoommateSearch());
        assertThrows(IllegalArgumentException.class,
                () -> registry.registerSource(new RuMeCandidateSource()));
        assertThrows(IllegalArgumentException.class,
                () -> registry.registerSearch(new RoommateSearch()));
    }

    @Test
    void rejectsNullRegistrations() {
        AppRegistry registry = new AppRegistry();
        assertThrows(IllegalArgumentException.class, () -> registry.registerSource(null));
        assertThrows(IllegalArgumentException.class, () -> registry.registerSearch(null));
    }

    @Test
    void supportsNewSourceAndNewAbstractionWithoutChangingRegistry() throws Exception {
        AppRegistry registry = new AppRegistry();
        registry.load();
        registry.registerSource(new ExtraSource());
        registry.registerSearch(new ExtraSearch());
        HousingSearch search = registry.getSearch("3");
        search.setSource(registry.getSource("extra"));
        assertEquals(901, search.search("Astana", 80000).get(0).getId());
        HousingSearch existingSearch = registry.getSearch("2");
        existingSearch.setSource(registry.getSource("extra"));
        assertEquals(1, existingSearch.search("Astana", 80000).size());
        search.setSource(registry.getSource("rume"));
        assertEquals(1, search.search("Astana", 80000).size());
    }

    @Test
    void changingMenuListsDoesNotChangeRegistry() {
        AppRegistry registry = new AppRegistry();
        registry.load();
        registry.getSourceCodes().clear();
        registry.getSearches().clear();
        assertEquals(3, registry.getSourceCodes().size());
        assertEquals(2, registry.getSearches().size());
    }
}
