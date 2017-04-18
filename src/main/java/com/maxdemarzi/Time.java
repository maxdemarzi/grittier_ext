package com.maxdemarzi;

import javax.ws.rs.QueryParam;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Time {
    public static final ZoneId utc = TimeZone.getTimeZone("UTC").toZoneId();

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy_MM_dd")
            .withZone(utc);


    public static Long getLatestTime(@QueryParam("since") Long since) {
        LocalDateTime dateTime;
        if (since == null) {
            dateTime = LocalDateTime.now(utc);
        } else {
            dateTime = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
        }
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
