package loader;

import ci646.weather.model.Location;
import ci646.weather.model.Record;
import ci646.weather.model.Sql2oModel;
import org.sql2o.Sql2o;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class Main {

    public static void main(String[] args) {

        Config conf         = ConfigFactory.load();

        String dbConnString = conf.getString("db.connectionString");
        String dbUser       = conf.getString("db.user");
        String dbPass       = conf.getString("db.password");
        // Set up the DAO
        Sql2o sql2o = new Sql2o(dbConnString, dbUser, dbPass);
        Sql2oModel model = new Sql2oModel(sql2o);
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        model.setupDatabase();
        InputStream is = Main.class.getResourceAsStream("/data/location.dat");
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(is))) {
            String line;
            String[] fields = null;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    // do nothing
                } else {
                    fields = line.split(",");
                    Location l = new Location(fields[0]+","+fields[1],
                            Float.parseFloat(fields[2].trim()),
                            Float.parseFloat(fields[3].trim()),
                            Float.parseFloat(fields[4].trim()));
                    log.info("Inserting: "+l.toString());
                    model.putLocation(l);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        is = Main.class.getResourceAsStream("/data/weather.dat");
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(is))) {
            String line;
            String[] fields = null;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    // do nothing
                } else {
                    fields = line.split(",");
                    int locID = model.getLocationsByName("Alberta, Canada").get().get(0).getLocID();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd hhmm");
                    Date parsedDate = dateFormat.parse(fields[0]);
                    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                    Record r = new Record(locID, timestamp, Float.parseFloat(fields[1].trim()),
                            Float.parseFloat(fields[2].trim()),
                            Float.parseFloat(fields[3].trim()),
                            Float.parseFloat(fields[4].trim()));
                    log.info("Inserting: "+r.toString());
                    model.putRecord(r);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
