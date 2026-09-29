package rume;

import java.util.ArrayList;
import java.util.List;

public class RuMeCandidateSource implements CandidateSource {
    private final List<Candidate> candidates = new ArrayList<>();

    public RuMeCandidateSource() {
        candidates.add(new Candidate(1, "Arman", "Astana", 80000));
        candidates.add(new Candidate(2, "Dana", "Astana", 100000));
        candidates.add(new Candidate(3, "Miras", "Astana", 60000));
        candidates.add(new Candidate(4, "Alina", "Almaty", 120000));
        candidates.add(new Candidate(5, "Timur", "Aktobe", 70000));
    }

    @Override
    public String getCode() {
        return "rume";
    }

    @Override
    public List<Candidate> findByCity(String city) throws CandidateSourceException {
        if (city == null || city.isBlank()) {
            throw new CandidateSourceException("City is required.");
        }
        List<Candidate> result = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (candidate.getCity().equalsIgnoreCase(city.trim())) {
                result.add(candidate);
            }
        }
        return result;
    }
}
