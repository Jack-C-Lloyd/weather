package ci646.weather.model;
/**
 * A POJO for weather record, which is a collection of data at a given location at a particular time.
 * Also the DAO (Data Access Object) used when interacting with the DB.
 */
import lombok.*;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
@ToString
public class Record {
    private long recordID;
    @NonNull
    private long locID;
    @NonNull
    private Timestamp date;
    @NonNull
    private float temperature;
    @NonNull
    private float humidity;
    @NonNull
    private float windSpeed;
    @NonNull
    private float windDirection;
}
