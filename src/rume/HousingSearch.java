package rume;

import java.util.ArrayList;
import java.util.List;

public abstract class HousingSearch {
    private CandidateSource source;

    public void setSource(CandidateSource source) {
        if (source == null) {
            throw new IllegalArgumentException("Candidate source is required.");
        }
        this.source = source;
    }

    public final List<Candidate> search(String city, int budget) throws CandidateSourceException {
        if (source == null) {
            throw new IllegalStateException("Select a candidate source before searching.");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required.");
        }
        if (budget <= 0) {
            throw new IllegalArgumentException("Budget must be positive.");
        }
        String requestedCity = city.trim();
        List<Candidate> candidates = source.findByCity(requestedCity);
        List<Candidate> matches = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (candidate.getCity().equalsIgnoreCase(requestedCity)
                    && matchesBudget(candidate.getBudget(), budget)) {
                matches.add(candidate);
            }
        }
        return matches;
    }

    public abstract String getCode();

    public abstract String getDescription();

    public abstract String getBudgetPrompt();

    protected abstract boolean matchesBudget(int candidateBudget, int requestedBudget);
}
