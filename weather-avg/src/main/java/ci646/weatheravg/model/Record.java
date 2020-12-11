package ci646.weatheravg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@ToString
public class Record {
    private long recordID;
    private long locID;
    private Timestamp date;
    private float temperature;
    private float humidity;
    private float windSpeed;
    private float windDirection;
}
