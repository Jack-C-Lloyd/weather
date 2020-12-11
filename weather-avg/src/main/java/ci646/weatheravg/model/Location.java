package ci646.weatheravg.model;
/**
 * A POJO for locations associated with weather records
 */

import lombok.*;

@Data
@AllArgsConstructor
@ToString
public class Location {
    private long locID;     // The Location ID
    private String name;    // The location name
    private float lat;      // The latitude of the location
    private float lon;      // The longitude of the location
    private float asl;      // The weather DB provides this data but I don't actually know what it represents ?!
}

