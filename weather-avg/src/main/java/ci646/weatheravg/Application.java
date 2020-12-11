package ci646.weatheravg;
/**
 * Entry point for the weather averages micro-service. It provides a REST API
 * That allows users to retrieve average values for locations (using the ID of that
 * location in the Weather DB) for:
 * + Every record ever made at that location,
 * + All records for a given month
 * + All records for a given day
 *
 * Currently only deals in temperature data.
 */

import ci646.weatheravg.model.Average;
import ci646.weatheravg.model.Location;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;

import static spark.Spark.*;

import ci646.weatheravg.model.Record;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

@Slf4j
public class Application {

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm").create();
    private static Config conf         = ConfigFactory.load();
    private static String weatherService = conf.getString("web.weatherService");

    public static void main(String[] args) {
        //Read in the config

        int port            = conf.getInt("web.port");
        String host         = conf.getString("web.host");

        // Configure Spark
        port(port);

        // Handle GET requests for the average of all data associated with a location
        get("/:locid/:type", "application/json", (req, res) -> {
            String locID = req.params(":locID");
            Average.TYPE t = typeFromString(req.params(":type"));
            log.info("received GET avg for location "+locID);
            // get all location info so we can return it with the result
            String locjson = readUrl(weatherService+"/locations/"+ locID);
            Location l = gson.fromJson(locjson, Location.class);

            // get the records from the weather-data service
            String json = readUrl(weatherService+"/records/"+ locID);
            Record[] temps = gson.fromJson(json, Record[].class);
            Average a = recordsToAverage(temps, t, l);
            return jsonify(Optional.of(a));
        });

        // Handle GET requests for the average of all data recorded at a given location in a given month
        get("/:loc/:type/:y/:m", "application/json", (req, res) -> {
            String locID = req.params(":loc");
            Average.TYPE t = typeFromString(req.params(":type"));
            String yearStr = req.params(":y");
            String monthStr = req.params(":m");
            log.info(String.format("received GET avg for location %s YEAR %s MONTH %s", locID, yearStr, monthStr));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            boolean isLY = LocalDate.parse(yearStr+"-"+monthStr+"-"+"01",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).isLeapYear();
            LocalDateTime startDate = LocalDateTime.parse(yearStr+"-"+monthStr+"-"+"01T00:00", dtf);
            LocalDateTime endDate = startDate.withDayOfMonth(
                    startDate.getMonth().length(isLY));

            String fromStr = startDate.format(dtf);
            String toStr = endDate.format(dtf);

            Average a = averageForRange(Long.parseLong(locID), t, fromStr, toStr);
            return jsonify(Optional.of(a));
        });

        // Handle GET requests for the average of all data recorded at a given location on a given day
        get("/:loc/:type/:y/:m/:d", "application/json", (req, res) -> {
            String locID = req.params(":loc");
            Average.TYPE t = typeFromString(req.params(":type"));
            String yearStr = req.params(":y");
            String monthStr = req.params(":m");
            String dayStr = req.params(":d");
            log.info(String.format("received GET avg for location %s YEAR %s MONTH %s DAY %s",
                    locID, yearStr, monthStr, dayStr));
            String fromStr = yearStr+"-"+monthStr+"-"+dayStr+"T00:00";
            String toStr = yearStr+"-"+monthStr+"-"+dayStr+"T23:59";
            Average a = averageForRange(Long.parseLong(locID), t, fromStr, toStr);
            return jsonify(Optional.of(a));
        });

    }

    private static Average.TYPE typeFromString(String type) {
        switch (type) {
            case "HU": return Average.TYPE.HUMIDITY;
            case "WS": return Average.TYPE.WIND_SPEED;
            case "WD": return Average.TYPE.WIND_DIRECTION;
            default: return Average.TYPE.TEMPERATURE;
        }
    }

    /**
     * Read the contents from a HTTP GET request to a URL
     * @param urlString
     * @return
     * @throws Exception
     */
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    /**
     * Turn an Optional object into a JSON String
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

    /**
     * Calculate the average (temperature) from an array of records.
     * @param records
     * @param l
     * @return
     */
    private static Average recordsToAverage(Record[] records, Average.TYPE t, Location l) {
        Function<Record, Float> f;
        switch (t) {
            case HUMIDITY: f = Record::getHumidity;
                break;
            case WIND_DIRECTION: f = Record::getWindDirection;
                break;
            case WIND_SPEED: f = Record::getWindSpeed;
                break;
            default:
                f = Record::getTemperature;
        }
        float sum = (float) Arrays.stream(records).mapToDouble((ToDoubleFunction<? super Record>) f).sum();
        Timestamp from = Arrays.stream(records).min(Comparator.comparing(Record::getDate))
                .orElseThrow(NoSuchElementException::new)
                .getDate();
        Timestamp to = Arrays.stream(records).max(Comparator.comparing(Record::getDate))
                .orElseThrow(NoSuchElementException::new)
                .getDate();
        float avg = sum / records.length;
        return new Average(l, from, to, avg, t);
    }

    /**
     * Retrieve the average (temperature) for a date range, where the beginning and
     * end of the range are represented as strings in the format yyyy-MM-ddTHH:mm
     * @param locID
     * @param fromStr
     * @param toStr
     * @return
     * @throws Exception
     */
    private static Average averageForRange(long locID, Average.TYPE t, String fromStr, String toStr)
            throws Exception {
        String locjson = readUrl(weatherService+"/locations/"+ locID);
        Location l = gson.fromJson(locjson, Location.class);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        boolean isLY = LocalDate.parse(fromStr.substring(0,10),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")).isLeapYear();
        LocalDateTime startDate = LocalDateTime.parse(fromStr, dtf);
        LocalDateTime endDate = LocalDateTime.parse(toStr, dtf);

        String json = null;
        json = readUrl(String.format("http://localhost:4567/records/%s/%s/%s", locID, fromStr, toStr));
        log.info(json);
        Record[] temps = gson.fromJson(json, Record[].class);
        return recordsToAverage(temps, t, l);
    }
}
