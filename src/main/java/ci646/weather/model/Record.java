package ci646.weather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@ToString
public class Record {
    private int locID;
    private Timestamp timestamp;
    private float temperature;
    private float humidity;
    private float windSpeed;
    private float windDirection;
}
