package ci646.weatheravg.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Average {
    Location location;
    Timestamp from;
    Timestamp to;
    float average;
    TYPE type;

    public static enum TYPE {TEMPERATURE, HUMIDITY, WIND_SPEED, WIND_DIRECTION};
}
