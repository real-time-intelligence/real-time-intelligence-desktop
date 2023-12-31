package ru.rti.desktop.model.chart;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jfree.data.xy.CategoryTableXYDataset;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.TimeZone;

@Log4j2
public class CategoryTableXYDatasetRealTime extends CategoryTableXYDataset {
    @Getter @Setter private HashMap<Integer, String> seriesNames;

    public CategoryTableXYDatasetRealTime() {
        seriesNames = new HashMap<>();
    }

    /**
     *  Add series value to time series stacked chart
     *
     * @param x time
     * @param y value
     * @param seriesName Category name
     */
    public void addSeriesValue(double x, double y, String seriesName){
        if (seriesNames.containsValue(seriesName)) {
            add(x, y, seriesName, true);
        } else {
            Integer key = !seriesNames.keySet().isEmpty() ?  Collections.max(seriesNames.keySet()) : 0;

            saveSeriesValues(key+1, seriesName);
            add(x, y, seriesName, true);
        }
    }

    public void saveSeriesValues(int series, String seriesName){
        seriesNames.put(series, seriesName);
    }

    public void deleteValuesFromDataset(int holdRange) {
        int imax = getItemCount();

        if (imax <= 0) {
            return;
        }

        Double xEndValue = (Double) getX(0, imax - 1);

        long begin = xEndValue.longValue() - ((long) holdRange * 60 * 1000);

        LocalDateTime dateBeginExpected = LocalDateTime.ofInstant(Instant.ofEpochMilli(begin),
            TimeZone.getDefault().toZoneId());

        for (int i=0; i<(imax - 1); i++) {
            try {
                Double xValue = (Double)getX(0, i);
                LocalDateTime dateBeginCurrent = LocalDateTime.ofInstant(Instant.ofEpochMilli(xValue.longValue()),
                    TimeZone.getDefault().toZoneId());
                if (dateBeginCurrent.isBefore(dateBeginExpected)) {
                    seriesNames.forEach((k,v)-> remove(xValue,v));
                } else {
                    break;
                }
            } catch (Exception e){
                log.catching(e);
            }
        }

    }

}
