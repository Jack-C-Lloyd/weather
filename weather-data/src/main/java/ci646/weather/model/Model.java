package ci646.weather.model;
/**
 * The model for our interactions with the DB. A class which implements this model
 * will need to know which DB we are using and its SQL dialect. Using an interface in this
 * way means that switching to another DB (e.g. a mock object for testing) means dropping in a
 * replacement that implements the same interface.
 */

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface Model {
    /* Store a new location. Returns the ID of the new location. */
    long putLocation(Location loc);
    /* Get a list of all locations. */
    Optional<List<Location>> getLocations();
    /* Get a location by its ID. */
    Optional<Location> getLocation(long locationID);
    /* Get a list of all locations whose name fuzzily matches the search term. */
    Optional<List<Location>> getLocationsByName(String name);
    /* Store a new record. Returns to ID of the new record. */
    long putRecord(Record rec);
    /* Get a record by its ID. */
    Optional<Record> getRecord(long id);
    /* Get a record by location ID and timestamp. */
    Optional<Record> getRecord(long locationID, Timestamp ts);
    /* Get a list of all records. */
    Optional<List<Record>> getRecords();
    /* Get a list of all records for a given location. */
    Optional<List<Record>> getRecords(long locationID);
    /* Get a list of all records for a given location and time range. */
    Optional<List<Record>> getRecords(long locationID, Timestamp from, Timestamp to);
}
