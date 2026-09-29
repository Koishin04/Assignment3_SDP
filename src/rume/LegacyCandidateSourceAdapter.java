package rume;

import java.util.ArrayList;
import java.util.List;

public class LegacyCandidateSourceAdapter implements CandidateSource {
    private final LegacyHousingService service;

    public LegacyCandidateSourceAdapter() {
        this(new LegacyHousingService());
    }

    public LegacyCandidateSourceAdapter(LegacyHousingService service) {
        if (service == null) {
            throw new IllegalArgumentException("Candidate service is required.");
        }
        this.service = service;
    }

    @Override
    public String getCode() {
        return "legacy";
    }

    @Override
    public List<Candidate> findByCity(String city) throws CandidateSourceException {
        if (city == null || city.isBlank()) {
            throw new CandidateSourceException("City is required.");
        }
        int cityCode = toCityCode(city.trim());
        if (cityCode == 0) {
            return new ArrayList<>();
        }
        LegacyResult response;
        try {
            response = service.loadProfiles(cityCode);
        } catch (RuntimeException error) {
            throw new CandidateSourceException("Candidate source failed.");
        }
        if (response == null) {
            throw new CandidateSourceException("Candidate source returned invalid data.");
        }
        if (response.getStatus() == LegacyHousingService.INVALID_REQUEST) {
            throw new CandidateSourceException("Candidate source rejected the request.");
        }
        if (response.getStatus() == LegacyHousingService.UNAVAILABLE) {
            throw new CandidateSourceException("Candidate source is unavailable.");
        }
        if (response.getStatus() != LegacyHousingService.SUCCESS) {
            throw new CandidateSourceException("Candidate source returned an unknown failure.");
        }
        try {
            List<Candidate> result = new ArrayList<>();
            for (String[] record : response.getRecords()) {
                if (record == null || record.length != 4) {
                    throw new IllegalArgumentException();
                }
                int recordCity = Integer.parseInt(record[2]);
                if (recordCity != cityCode) {
                    throw new IllegalArgumentException();
                }
                result.add(new Candidate(Integer.parseInt(record[0]), record[1],
                        toCityName(recordCity), Integer.parseInt(record[3])));
            }
            return result;
        } catch (RuntimeException error) {
            throw new CandidateSourceException("Candidate source returned invalid data.");
        }
    }

    private int toCityCode(String city) {
        if (city.equalsIgnoreCase("Astana")) {
            return 1;
        }
        if (city.equalsIgnoreCase("Almaty")) {
            return 2;
        }
        if (city.equalsIgnoreCase("Aktobe")) {
            return 3;
        }
        return 0;
    }

    private String toCityName(int code) {
        if (code == 1) {
            return "Astana";
        }
        if (code == 2) {
            return "Almaty";
        }
        if (code == 3) {
            return "Aktobe";
        }
        throw new IllegalArgumentException();
    }
}
