package ci646.weather.model;

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
        Map<String, String> colMaps = new HashMap<String,String>();
        colMaps.put("LOC_ID", "locID");
        colMaps.put("TS", "timestamp");
        colMaps.put("WIND_SPEED", "windSpeed");
        colMaps.put("WIND_DIRECTION", "windDirection");
        sql2o.setDefaultColumnMappings(colMaps);
    }

    @Override
    public void setupDatabase() {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("DROP TABLE IF EXISTS records")
                    .executeUpdate();
            conn.createQuery("DROP TABLE IF EXISTS locations")
                    .executeUpdate();
            conn.createQuery("CREATE TABLE locations ( loc_id INTEGER PRIMARY KEY, " +
                    "name VARCHAR(200) NOT NULL, " +
                    "lat REAL NOT NULL, " +
                    "lon REAL NOT NULL, " +
                    "asl REAL NOT NULL );")
                    .executeUpdate();
            conn.createQuery("CREATE TABLE records ( loc_id INTEGER NOT NULL, " +
                    "ts TIMESTAMP NOT NULL, " +
                    "temperature REAL NOT NULL, " +
                    "humidity REAL NOT NULL, " +
                    "wind_direction REAL NOT NULL, " +
                    "wind_speed REAL NOT NULL, " +
                    "FOREIGN KEY(loc_id) REFERENCES locations(loc_id) );")
                    .executeUpdate();
        }
    }

    @Override
    public void putLocation(Location loc) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("INSERT INTO locations(name, lat, lon, asl) " +
                    "VALUES (:name, :lat, :lon, :asl)")
                    .addParameter("name", loc.getName())
                    .addParameter("lat", loc.getLat())
                    .addParameter("lon", loc.getLon())
                    .addParameter("asl", loc.getAsl())
                    .executeUpdate();
        }
    }

    @Override
    public Optional<Location> getLocation(long locationID) {
        try (Connection conn = sql2o.open()) {
            List<Location> result = conn.createQuery(
                    "SELECT name, lat, lon, asl FROM locations WHERE loc_id = :id")
                    .addParameter("id", locationID)
                    .executeAndFetch(Location.class);
            Optional<Location> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result.get(0));
            }
            return l;
        }
    }

    @Override
    public Optional<List<Location>> getLocationsByName(String name) {
        try (Connection conn = sql2o.open()) {
            List<Location> result = conn.createQuery(
                    "SELECT name, lat, lon, asl FROM locations WHERE name = :name")
                    .addParameter("name", name)
                    .executeAndFetch(Location.class);
            Optional<List<Location>> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result);
            }
            return l;
        }
    }

    @Override
    public Optional<List<Location>> getLocations() {
        try (Connection conn = sql2o.open()) {
            List<Location> result = conn.createQuery(
                    "SELECT name, lat, lon, asl FROM locations")
                    .executeAndFetch(Location.class);
            Optional<List<Location>> l = Optional.empty();
            if (result.size() > 0) {
                l = Optional.of(result);
            }
            return l;
        }
    }

    @Override
    public void putRecord(Record rec) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("INSERT INTO records(loc_id, ts, temperature, humidity, wind_speed, wind_direction) " +
                    "VALUES (:locid, :ts, :temp, :hum, :ws, :wd)")
                    .addParameter("locid", rec.getLocID())
                    .addParameter("ts", rec.getTimestamp())
                    .addParameter("temp", rec.getTemperature())
                    .addParameter("hum", rec.getHumidity())
                    .addParameter("ws", rec.getWindSpeed())
                    .addParameter("wd", rec.getWindDirection())
                    .executeUpdate();
        }
    }

    @Override
    public Optional<Record> getRecord(long locationID, Timestamp ts) {
        try (Connection conn = sql2o.open()) {
            List<Record> result = conn.createQuery(
                    "SELECT loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
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

    @Override
    public Optional<List<Record>> getRecords(long locationID) {
        try (Connection conn = sql2o.open()) {
            List<Record> result = conn.createQuery(
                    "SELECT loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
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

    @Override
    public Optional<List<Record>> getRecords(long locationID, Timestamp from, Timestamp to) {
        try (Connection conn = sql2o.open()) {
            List<Record> result = conn.createQuery(
                    "SELECT loc_id, ts, temperature, humidity, wind_speed, wind_direction " +
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
