package rume;

import java.util.List;

public interface CandidateSource {
    String getCode();

    List<Candidate> findByCity(String city) throws CandidateSourceException;
}
