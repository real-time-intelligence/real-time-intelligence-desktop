package ru.rti.desktop.helper;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Log4j2
@Data
public class ReportHelper {
    private String formatPattern = "dd-MM-yyyy HH:mm:ss";
    private String formatFusedPattern = "ddMMyyHHmmss";

    public ReportHelper() {
    }

    public String getDateFormat(long date){
        return Instant.ofEpochMilli(date)
                .atZone(ZoneId.systemDefault())
                .format(getDateTimeFormatter())
                .toString();
    }

    public String getDateFormatFused(long date){
        return  Instant.ofEpochMilli(date)
                .atZone(ZoneId.systemDefault())
                .format(getDateTimeFormatterFused())
                .toString();
    }

    public DateTimeFormatter getDateTimeFormatter(){
        return DateTimeFormatter.ofPattern(formatPattern);
    }

    public DateTimeFormatter getDateTimeFormatterFused(){
        return DateTimeFormatter.ofPattern(formatFusedPattern);
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public String getFormatFusedPattern() {
        return formatFusedPattern;
    }
}
