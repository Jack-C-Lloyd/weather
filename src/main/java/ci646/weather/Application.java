package ci646.weather;

import ci646.weather.model.Model;
import ci646.weather.model.Record;
import ci646.weather.model.Location;
import ci646.weather.model.Sql2oModel;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Sql2o;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

@Slf4j
public class Application {
    //Our Data Access Object (DAO)
    private static Model model = null;

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
            if(or.isEmpty()) {
                return new Gson().toJson("");
            } else {
                return new Gson().toJson(or.get());
            }
        });

        // Handle GET requests for named locations -- JSON
        get("/locations/:name", "application/json", (req, res) -> {
            String name = req.params(":name");
            log.info("received GET locations/"+name);
            Optional<List<Location>> or = model.getLocationsByName(name);

            if(or.isEmpty()) {
                return new Gson().toJson("");
            } else {
                return new Gson().toJson(or.get());
            }
        });

        /*
        Records
         */
        // Handle GET requests for records at a given location -- JSON
        get("/records/:loc", "application/json", (req, res) -> {
            log.info("received GET requests");
            int id = Integer.parseInt(req.params(":loc"));
            Optional<List<Record>> or = model.getRecords(id);
            if(or.isEmpty()) {
                return new Gson().toJson("");
            } else {
                return new Gson().toJson(or.get());
            }
        });
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
