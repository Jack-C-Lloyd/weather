package ci646.weather.model;

import lombok.*;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
@ToString
public class Location {
    private long locID;
    @NonNull
    private String name;
    @NonNull
    private float lat;
    @NonNull
    private float lon;
    @NonNull
    private float asl;
}
