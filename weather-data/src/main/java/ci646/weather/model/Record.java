package ci646.weather.model;

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
