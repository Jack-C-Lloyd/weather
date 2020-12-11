package ci646.weatheravg.model;

import lombok.*;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@ToString
public class Location {
    private long locID;
    private String name;
    private float lat;
    private float lon;
    private float asl;
}

