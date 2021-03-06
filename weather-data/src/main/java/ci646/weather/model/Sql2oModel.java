package ci646.weather.model;
/**
 * The Object-Relational Mapping, using the Sql2o framework. Written for an SQLite database.
 */

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Sql2oModel implements Model {

    private Sql2o sql2o;

    public Sql2oModel(Sql2o sql2o) {
        this.sql2o = sql2o;
        // The mapping from column names in the DB to field names in the DAOs.
        // Onle need to be supplied when they differ.
        Map<String, String> colMaps = new HashMap<String,String>();
        colMaps.put("LOC_ID", "locID");
        colMaps.put("RECORD_ID", "recordID");
        colMaps.put("TS", "date");
        colMaps.put("WIND_SPEED", "windSpeed");
        colMaps.put("WIND_DIRECTION", "windDirection");
        sql2o.setDefaultColumnMappings(colMaps);
    }

    /**
     * Store a new location in the DB. Returns the ID of the new location.
     * @param loc
     * @return
     */
    @Override
    public long putLocation(Location loc) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("INSERT INTO locations(name, lat, lon, asl) " +
                    "VALUES (:name, :lat, :lon, :asl)")
                    .addParameter("name", loc.getName())
                    .addParameter("lat", loc.getLat())
                    .addParameter("lon", loc.getLon())
                    .addParameter("asl", loc.getAsl())
                    .executeUpdate();
            return Long.parseLong(conn.createQuery("SELECT last_insert_rowid()")
                    .executeScalar().toString());
        }
    }

    /**
     * Get a location based on its ID.
     * @param locationID
     * @return
     */
    @Override
    public Optional<Location> getLocation(long locationID) {
        try (Connection conn = sql2o.open()) {
            List<Location> result = conn.createQuery(
                    "SELECT loc_id, name, lat, lon, asl " +
                            "FROM locations " +
                            "WHERE loc_id = :id")
                    .addParameter("id", locationID)
                    .executeAndFetch(Location.class);
            Optional<Location> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result.get(0));
            }
            return l;
        }
    }

    /**
     * Get all locations whose name matches a search term.
     * @param name
     * @return
     */
    @Override
    public Optional<List<Location>> getLocationsByName(String name) {
        try (Connection conn = sql2o.open()) {
            List<Location> result = conn.createQuery(
                    "SELECT loc_id, name, lat, lon, asl FROM locations WHERE name LIKE :name")
                    .addParameter("name", "%"+name+"%")
                    .executeAndFetch(Location.class);
            Optional<List<Location>> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result);
            }
            return l;
        }
    }

    /**
     * Get all locations.
     * @return
     */
    @Override
    public Optional<List<Location>> getLocations() {
        try (Connection conn = sql2o.open()) {
            List<Location> result = conn.createQuery(
                    "SELECT loc_id, name, lat, lon, asl FROM locations")
                    .executeAndFetch(Location.class);
            Optional<List<Location>> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result);
            }
            return l;
        }
    }

    /**
     * Store a new record. Returns the ID of the new record.
     * @param rec
     * @return
     */
    @Override
    public long putRecord(Record rec) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("INSERT INTO records(loc_id, ts, temperature, humidity, wind_speed, wind_direction) " +
                    "VALUES (:locid, :ts, :temp, :hum, :ws, :wd)")
                    .addParameter("locid", rec.getLocID())
                    .addParameter("ts", rec.getDate())
                    .addParameter("temp", rec.getTemperature())
                    .addParameter("hum", rec.getHumidity())
                    .addParameter("ws", rec.getWindSpeed())
                    .addParameter("wd", rec.getWindDirection())
                    .executeUpdate();
            return Long.parseLong(conn.createQuery("SELECT last_insert_rowid()")
                    .executeScalar().toString());
        }
    }

    /**
     * Retrieve a record by its ID.
     * @param id
     * @return
     */
    @Override
    public Optional<Record> getRecord(long id) {
        try (Connection conn = sql2o.open()) {
            Record result = conn.createQuery(
                    "SELECT record_id, loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
                            "FROM records " +
                            "WHERE record_id = :record_id")
                    .addParameter("record_id", id)
                    .executeAndFetchFirst(Record.class);
            return Optional.of(result);
        }
    }

    /**
     * Retrieve a record by its Location ID and timestamp.
     * @param locationID
     * @param ts
     * @return
     */
    @Override
    public Optional<Record> getRecord(long locationID, Timestamp ts) {
        try (Connection conn = sql2o.open()) {
            List<Record> result = conn.createQuery(
                    "SELECT record_id, loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
                            "FROM records " +
                            "WHERE loc_id = :loc_id AND ts = :ts")
                    .addParameter("loc_id", locationID)
                    .addParameter("ts", ts)
                    .executeAndFetch(Record.class);
            Optional<Record> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result.get(0));
            }
            return l;
        }
    }

    /**
     * Get all records.
     * @return
     */
    @Override
    public Optional<List<Record>> getRecords() {
        try (Connection conn = sql2o.open()) {
            List<Record> result = conn.createQuery(
                    "SELECT record_id, loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
                            "FROM records ")
                    .executeAndFetch(Record.class);
            Optional<List<Record>> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result);
            }
            return l;
        }
    }

    /**
     * Get all records at a given location.
     * @param locationID
     * @return
     */
    @Override
    public Optional<List<Record>> getRecords(long locationID) {
        try (Connection conn = sql2o.open()) {
            List<Record> result = conn.createQuery(
                    "SELECT record_id, loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
                            "FROM records " +
                            "WHERE loc_id = :loc_id")
                    .addParameter("loc_id", locationID)
                    .executeAndFetch(Record.class);
            Optional<List<Record>> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result);
            }
            return l;
        }
    }

    /**
     * Get all records at a given location within a given time range.
     * @param locationID
     * @param from
     * @param to
     * @return
     */
    @Override
    public Optional<List<Record>> getRecords(long locationID, Timestamp from, Timestamp to) {
        try (Connection conn = sql2o.open()) {
            List<Record> result = conn.createQuery(
                    "SELECT record_id, loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
                            "FROM records " +
                            "WHERE loc_id = :loc_id AND ts BETWEEN :from AND :to")
                    .addParameter("loc_id", locationID)
                    .addParameter("from", from)
                    .addParameter("to", to)
                    .executeAndFetch(Record.class);
            Optional<List<Record>> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result);
            }
            return l;
        }
    }
}
