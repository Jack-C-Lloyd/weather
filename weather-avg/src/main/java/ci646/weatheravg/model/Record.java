package ci646.weatheravg.model;
/**
 * A POJO for weather record, which is a collection of data at a given location at a particular time.
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@ToString
public class Record {
    private long recordID;          // The record ID
    private long locID;             // The location ID
    private Timestamp date;         // The time of the recording
    private float temperature;      // The temperature in Celsius
    private float humidity;         // The humidity -- units?
    private float windSpeed;        // The windspeed in mph
    private float windDirection;    // The wind direction in degrees from North
}
