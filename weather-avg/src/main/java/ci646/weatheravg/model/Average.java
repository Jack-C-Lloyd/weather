package ci646.weatheravg.model;
/**
 * A POJO to store data on an average value calculated from weather records.
 */

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Average {
    Location location;  // The location the average value relates to
    Timestamp from;     // The beginning of the date range
    Timestamp to;       // The end of the date range
    float average;      // The average value
    TYPE type;          // What type of data is this the average of?
    // Types of data that we create averages of
    public static enum TYPE {TEMPERATURE, HUMIDITY, WIND_SPEED, WIND_DIRECTION};
}
