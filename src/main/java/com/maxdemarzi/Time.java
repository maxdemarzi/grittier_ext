package com.maxdemarzi;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Time {
    public static final ZoneId utc = TimeZone.getTimeZone("UTC").toZoneId();

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy_MM_dd")
            .withZone(utc);
}
