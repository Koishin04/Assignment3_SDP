package rume;

import java.util.ArrayList;
import java.util.List;

public class LegacyHousingService {
    public static final int SUCCESS = 0;
    public static final int INVALID_REQUEST = 1;
    public static final int UNAVAILABLE = 2;

    private final boolean available;
    private final String[][] records = {
            {"201", "Nursultan", "1", "80000"},
            {"202", "Aigerim", "1", "110000"},
            {"203", "Ilyas", "2", "120000"},
            {"204", "Anel", "3", "70000"}
    };

    public LegacyHousingService() {
        this(true);
    }

    public LegacyHousingService(boolean available) {
        this.available = available;
    }

    public LegacyResult loadProfiles(int cityCode) {
        if (!available) {
            return new LegacyResult(UNAVAILABLE, null);
        }
        if (cityCode < 1 || cityCode > 3) {
            return new LegacyResult(INVALID_REQUEST, null);
        }
        List<String[]> result = new ArrayList<>();
        for (String[] record : records) {
            if (record[2].equals(String.valueOf(cityCode))) {
                result.add(record.clone());
            }
        }
        return new LegacyResult(SUCCESS, result.toArray(new String[0][]));
    }
}
