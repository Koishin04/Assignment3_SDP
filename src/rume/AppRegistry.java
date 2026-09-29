package rume;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

public class AppRegistry {
    private final Map<String, CandidateSource> sources = new LinkedHashMap<>();
    private final Map<String, HousingSearch> searches = new LinkedHashMap<>();

    public void load() {
        for (CandidateSource source : ServiceLoader.load(CandidateSource.class)) {
            registerSource(source);
        }
        for (HousingSearch search : ServiceLoader.load(HousingSearch.class)) {
            registerSearch(search);
        }
        if (sources.isEmpty() || searches.isEmpty()) {
            throw new IllegalStateException("No providers found. Add resources to the classpath.");
        }
    }

    public void registerSource(CandidateSource source) {
        if (source == null) {
            throw new IllegalArgumentException("Candidate source is required.");
        }
        String code = normalize(source.getCode());
        if (sources.containsKey(code)) {
            throw new IllegalArgumentException("Duplicate catalog code.");
        }
        sources.put(code, source);
    }

    public void registerSearch(HousingSearch search) {
        if (search == null) {
            throw new IllegalArgumentException("Housing search is required.");
        }
        String code = normalize(search.getCode());
        if (searches.containsKey(code)) {
            throw new IllegalArgumentException("Duplicate search code.");
        }
        searches.put(code, search);
    }

    public CandidateSource getSource(String code) {
        CandidateSource source = sources.get(normalize(code));
        if (source == null) {
            throw new IllegalArgumentException("Unknown catalog code.");
        }
        return source;
    }

    public HousingSearch getSearch(String code) {
        HousingSearch search = searches.get(normalize(code));
        if (search == null) {
            throw new IllegalArgumentException("Unknown search code.");
        }
        return search;
    }

    public List<String> getSourceCodes() {
        return new ArrayList<>(sources.keySet());
    }

    public List<HousingSearch> getSearches() {
        return new ArrayList<>(searches.values());
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Selection code is required.");
        }
        return code.trim().toLowerCase(Locale.ROOT);
    }
}
