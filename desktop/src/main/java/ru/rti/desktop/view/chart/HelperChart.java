package ru.rti.desktop.view.chart;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.fbase.model.output.StackedColumn;
import ru.rti.desktop.model.chart.ChartRange;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.RangeChartHistory;

public interface HelperChart {

    default long getRangeRealTime(ChartInfo chartInfo) {
        return ((long) chartInfo.getRange() * 60 * 1000);
    }

    default long getRangeHistory(ChartInfo chartInfo) {
        if (RangeChartHistory.DAY.equals(chartInfo.getRangeHistory())) {
            return 24L * 60 * 60 * 1000;
        }
        if (RangeChartHistory.WEEK.equals(chartInfo.getRangeHistory())) {
            return 7L * 24 * 60 * 60 * 1000;
        }
        if (RangeChartHistory.MONTH.equals(chartInfo.getRangeHistory())) {
            return 30L * 24 * 60 * 60 * 1000;
        } else {
            return (chartInfo.getCustomEnd() - chartInfo.getCustomBegin());
        }
    }

    default ChartRange getRange(ChartInfo chartInfo) {

        ChartRange chartRange = new ChartRange();

        if (RangeChartHistory.CUSTOM.equals(chartInfo.getRangeHistory())) {
            chartRange.setEnd(chartInfo.getCustomEnd());
        } else {
            LocalDateTime startOfDay = LocalDate.now().plusDays(1).atStartOfDay();
            chartRange.setEnd(startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        chartRange.setBegin(chartRange.getEnd() - getRangeHistory(chartInfo));

        return chartRange;
    }

    default LocalDateTime toLocalDateTimeOfEpochMilli(long ofEpochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ofEpochMilli), TimeZone.getDefault().toZoneId());
    }

}
