package ci646.weather.model;
/**
 * A POJO for locations associated with weather records, also the DAO
 * (Data Access Object) that we use when interacting with the DB.
 */
import lombok.*;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
@ToString
public class Location {
    private long locID;     // The location ID -- this is nullable so we can create new objects before storing them in the DB
    @NonNull
    private String name;    // The name of the location
    @NonNull
    private float lat;      // The latitude of the location
    @NonNull
    private float lon;      // The logitude of the location
    @NonNull
    private float asl;      // The weather DB provides this data but I don't know what it means!?
}
