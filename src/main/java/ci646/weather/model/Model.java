package ci646.weather.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface Model {
    void setupDatabase();
    void putLocation(Location loc);
    Optional<List<Location>> getLocations();
    Optional<Location> getLocation(long locationID);
    Optional<List<Location>> getLocationsByName(String name);
    void putRecord(Record rec);
    Optional<Record> getRecord(long locationID, Timestamp ts);
    Optional<List<Record>> getRecords(long locationID);
    Optional<List<Record>> getRecords(long locationID, Timestamp from, Timestamp to);
}
