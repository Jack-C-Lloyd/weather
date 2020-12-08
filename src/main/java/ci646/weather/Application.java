package ci646.weather;

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
import netscape.javascript.JSObject;
import org.sql2o.Sql2o;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
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
        String HOME         = host +":"+port;
        String staticLoc    = conf.getString("web.staticFiles");
        String templatePath = conf.getString("web.templatePath");
        String indexPath    = templatePath + "index.vm";
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

        // Handle GET requests for named locations -- JSON
        get("/locations", "application/json", (req, res) -> {
            log.info("received GET locations");
            Optional<List<Location>> or = model.getLocations();
            return jsonify(or);
        });

        // Handle POST requests for locations -- JSON
        post("/locations", "application/json", (req, res) -> {
            log.info(req.queryString());
            Location l = new Location(req.queryParams("name")
                    , Float.parseFloat(req.queryParams("lat"))
                    , Float.parseFloat(req.queryParams("lon"))
                    , Float.parseFloat(req.queryParams("asl")));
            log.info("received PUT location");
            long id = model.putLocation(l);
            return jsonify(model.getLocation(id));
        });

        // Handle GET requests for named locations -- JSON
        get("/locations/:name", "application/json", (req, res) -> {
            String name = req.params(":name");
            log.info("received GET locations/"+name);
            Optional<List<Location>> or = model.getLocationsByName(name);
            return jsonify(or);
        });

        /*
        Records
         */

        // Handle GET requests for records at a given location -- JSON
        get("/records/:loc", "application/json", (req, res) -> {
            log.info("received GET records FOR LOCATION");
            long id = Long.parseLong(req.params(":loc"));
            Optional<List<Record>> or = model.getRecords(id);
            return jsonify(or);
        });

        // Handle GET requests for records at a given location -- JSON
        get("/records/:loc/:from/:to", "application/json", (req, res) -> {
            log.info("received GET records FOR LOCATION FROM TO");
            long id = Long.parseLong(req.params(":loc"));
            try {
                Timestamp from = Timestamp.from(LocalDateTime.parse(req.params(":from")).toInstant(ZoneOffset.UTC));
                Timestamp to = Timestamp.from(LocalDateTime.parse(req.params(":to")).toInstant(ZoneOffset.UTC));
                Optional<List<Record>> or = model.getRecords(id, from, to);
                return jsonify(or);
            } catch (DateTimeParseException e) {
                return new Gson().toJson(e);
            }
        });

        // Handle POST requests for records -- JSON
        post("/records/:loc", "application/json", (req, res) -> {
            log.info(req.queryString());
            long loc = Long.parseLong(req.params(":loc"));
            Timestamp ts = Timestamp.from(LocalDateTime.parse(req.queryParams("ts")).toInstant(ZoneOffset.UTC));
            Record r = new Record(loc
                    , ts
                    , Float.parseFloat(req.queryParams("temp"))
                    , Float.parseFloat(req.queryParams("hum"))
                    , Float.parseFloat(req.queryParams("ws"))
                    , Float.parseFloat(req.queryParams("wd")));
            log.info("received PUT record");
            long id = model.putRecord(r);
            return jsonify(model.getRecord(id));
        });
    }

    private static <T> String jsonify(Optional<T> o) {
        if (o == null || o.isEmpty()) {
            return gson.toJson(new JsonObject());
        } else {
            return gson.toJson(o.get());
        }
    }

    /**
     * Helper method to render a velocity template
     * @param model
     * @param templatePath
     * @return
     */
    private static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
