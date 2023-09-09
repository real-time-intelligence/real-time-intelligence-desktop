package ru.rti.desktop.manager;

import ru.rti.desktop.model.report.QueryReportData;

import java.io.IOException;
import java.util.Map;

public interface ReportManager {

    void addConfig(Map<String, QueryReportData> config, String formattedDate);

    Map<String ,QueryReportData> getConfig(String dirName, String fileName);

    void deleteDesign(String configName) throws IOException;
}
