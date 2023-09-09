package ru.rti.desktop.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryReportData {

    private List<CProfileReport> cProfileReportList = new ArrayList<>();
    private List<MetricReport> metricReportList = new ArrayList<>();


}
