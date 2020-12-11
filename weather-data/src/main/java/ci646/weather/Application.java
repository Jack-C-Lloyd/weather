package ci646.weather;
/**
 * Entry point for the weather data micro-service. It provides a REST API
 * That allows users to retrieve data recording for locations (using the ID or name of that
 * location in the Weather DB, the ID of a particular record, or the combination of a location and a time).
 *
 */

import ci646.weather.model.Model;
import ci646.weather.model.Record;
import ci646.weather.model.Location;
import ci646.weather.model.Sql2oModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Sql2o;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

@Slf4j
public class Application {
    //Our Data Access Object (DAO)
    private static Model model = null;
    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm").create();

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        //Read in the config
        Config conf         = ConfigFactory.load();
        int port            = conf.getInt("web.port");
        String host         = conf.getString("web.host");
        String staticLoc    = conf.getString("web.staticFiles");
        String templatePath = conf.getString("web.templatePath");
        long staticTO       = conf.getLong("web.staticTimeout");
        String dbConnString = conf.getString("db.connectionString");
        String dbUser       = conf.getString("db.user");
        String dbPass       = conf.getString("db.password");

        // Configure Spark
        port(port);
        staticFiles.location(staticLoc);
        staticFiles.expireTime(staticTO);

        // Set up the DAO
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Sql2o sql2o = new Sql2o(dbConnString, dbUser, dbPass);
        model = new Sql2oModel(sql2o);

        // Define the routes

        /*
        Locations
         */

        // Handle GET requests for all locations.
        get("/locations", "application/json", (req, res) -> {
            log.info("received GET locations");
            Optional<List<Location>> or = model.getLocations();
            return jsonify(or);
        });

        // Handle POST requests to put a new location. Returns the location created.
        post("/locations", "application/json", (req, res) -> {
            log.info(req.queryString());
            Location l = new Location(req.queryParams("name")
                    , Float.parseFloat(req.queryParams("lat"))
                    , Float.parseFloat(req.queryParams("lon"))
                    , Float.parseFloat(req.queryParams("asl")));
            log.info("received POST location");
            long id = model.putLocation(l);
            return jsonify(model.getLocation(id));
        });

        // Handle GET requests for named locations and IDs -- can handle a string param (which search for the location
        // name) or a numeric ID.
        get("/locations/:name", "application/json", (req, res) -> {
            String name = req.params(":name");
            log.info("received GET locations/"+name);
            try {
                long id = Long.parseLong(name);
                Optional<Location> or = model.getLocation(id);
                return jsonify(or);
            } catch (NumberFormatException nfe) {
                Optional<List<Location>> or = model.getLocationsByName(name);
                return jsonify(or);
            }
        });

        /*
        Records
         */

        // Handle GET requests for all records.
        get("/records", "application/json", (req, res) -> {
            log.info("received GET records");
            Optional<List<Record>> or = model.getRecords();
            return jsonify(or);
        });

        // Handle GET requests for records at a given location.
        get("/records/:loc", "application/json", (req, res) -> {
            long id = Long.parseLong(req.params(":loc"));
            log.info("received GET records FOR LOCATION "+id);
            Optional<List<Record>> or = model.getRecords(id);
            return jsonify(or);
        });

        // Handle GET requests for records at a given location in a time range.
        get("/records/:loc/:from/:to", "application/json", (req, res) -> {
            String locStr = req.params(":loc");
            String fromStr = req.params(":from");
            String toStr = req.params(":to");
            log.info(String.format("received GET records FOR LOCATION %s FROM %s TO %s",
                    locStr, fromStr, toStr));
            long id = Long.parseLong(locStr);
            // Try to parse the timestamps, could fail
            try {
                Timestamp from = Timestamp.from(LocalDateTime.parse(fromStr).toInstant(ZoneOffset.UTC));
                Timestamp to = Timestamp.from(LocalDateTime.parse(toStr).toInstant(ZoneOffset.UTC));
                Optional<List<Record>> or = model.getRecords(id, from, to);
                return jsonify(or);
            } catch (DateTimeParseException e) {
                return new Gson().toJson(e);
            }
        });

        // Handle POST requests for a new record. Returns the new record.
        post("/records/:loc", "application/json", (req, res) -> {
            long loc = Long.parseLong(req.params(":loc"));
            Timestamp ts = Timestamp.from(LocalDateTime.parse(req.queryParams("ts")).toInstant(ZoneOffset.UTC));
            Record r = new Record(loc
                    , ts
                    , Float.parseFloat(req.queryParams("temp"))
                    , Float.parseFloat(req.queryParams("hum"))
                    , Float.parseFloat(req.queryParams("ws"))
                    , Float.parseFloat(req.queryParams("wd")));
            long id = model.putRecord(r);
            log.info(String.format("PUT record ID %d FOR LOCATION %d",
                    id, loc));
            return jsonify(model.getRecord(id));
        });
    }

    /**
     * Helper method to turn Optional objects into JSON strings.
     * @param o
     * @param <T>
     * @return
     */
    private static <T> String jsonify(Optional<T> o) {
        if (o == null || o.isEmpty()) {
            return gson.toJson(new JsonObject());
        } else {
            return gson.toJson(o.get());
        }
    }
}
