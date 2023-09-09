package ru.rti.desktop.manager.impl;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.helper.GsonHelper;
import ru.rti.desktop.manager.ReportManager;
import ru.rti.desktop.model.report.QueryReportData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Log4j2
@Singleton
public class ReportManagerImpl implements ReportManager {
    private final GsonHelper gsonHelper;

    @Inject
    public ReportManagerImpl(GsonHelper gsonHelper) {
        this.gsonHelper = gsonHelper;
    }


    @Override
    public void addConfig(Map<String, QueryReportData> config, String formattedDate) {
        try {
            gsonHelper.add(config, formattedDate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String ,QueryReportData> getConfig(String dirName, String fileName) {
        return gsonHelper.getConfig(dirName, fileName);
    }

    @Override
    public void deleteDesign(String configName) throws IOException {
        gsonHelper.deleteDesign(configName);
    }


}
