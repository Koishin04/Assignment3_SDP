package rume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UniversityCandidateSource implements CandidateSource {
    private final Map<String, List<Candidate>> candidates = new HashMap<>();

    public UniversityCandidateSource() {
        candidates.put("astana", List.of(
                new Candidate(101, "Aruzhan", "Astana", 80000),
                new Candidate(102, "Dias", "Astana", 90000)));
        candidates.put("almaty", List.of(new Candidate(103, "Madina", "Almaty", 100000)));
        candidates.put("aktobe", List.of(new Candidate(104, "Sanzhar", "Aktobe", 60000)));
    }

    @Override
    public String getCode() {
        return "university";
    }

    @Override
    public List<Candidate> findByCity(String city) throws CandidateSourceException {
        if (city == null || city.isBlank()) {
            throw new CandidateSourceException("City is required.");
        }
        String key = city.trim().toLowerCase(Locale.ROOT);
        List<Candidate> result = candidates.get(key);
        if (result == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(result);
    }
}
