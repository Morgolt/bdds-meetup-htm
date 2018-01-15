package com.epam.bdcc.meetup.htm;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.numenta.nupic.util.Tuple;

import java.util.HashMap;
import java.util.Map;

public class ResultRecord {
    public static final String HEADER = "lat,long,speed,trip_id,time,anomalyScore\n";

    private String latitude;
    private String longitude;
    private String speed;

    public String getTripId() {
        return tripId;
    }

    private String tripId;
    private String time;
    private double anomalyScore;

    private ResultRecord(String latitude,
                         String longitude,
                         String speed,
                         String time,
                         String tripId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.time = time;
        this.tripId = tripId;
    }

    public static ResultRecord parseLine(String line) {
        String[] fields = line.split(",");
        return new ResultRecord(
                fields[FieldMapping.LATITUDE.pos],
                fields[FieldMapping.LONGITUDE.pos],
                fields[FieldMapping.SPEED.pos],
                fields[FieldMapping.TIMESTAMP.pos],
                fields[FieldMapping.TRIP_ID.pos]
        );
    }

    @Override
    public String toString() {
        return latitude + ',' +
                longitude + ',' +
                speed + ',' +
                tripId + ',' +
                time + ',' +
                anomalyScore + '\n';
    }

    public Map<String, Object> getAnomalyDetectionFeatures() {
        double lon = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);
        double speed = Double.parseDouble(this.speed) / 3.6;

        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> record = new HashMap<>();
        record.put("ts", format.parseDateTime(time));
        record.put("location", new Tuple(lon, lat, speed));
        return record;
    }

    public double getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }
}

enum FieldMapping {
    TRIP_ID(0),
    LATITUDE(1),
    LONGITUDE(2),
    TIMESTAMP(3),
    SPEED(4);

    final int pos;

    FieldMapping(int pos) {
        this.pos = pos;
    }
}
