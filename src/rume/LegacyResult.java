package rume;

public final class LegacyResult {
    private final int status;
    private final String[][] records;

    public LegacyResult(int status, String[][] records) {
        this.status = status;
        this.records = records;
    }

    public int getStatus() {
        return status;
    }

    public String[][] getRecords() {
        return records;
    }
}
