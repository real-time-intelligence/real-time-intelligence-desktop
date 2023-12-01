package ru.rti.desktop.view.handler.report.design;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.chart.HelperChart;


@Log4j2
public abstract class ChartReportHandler implements HelperChart {

    protected ExecutorService executorService;
    protected final ProfileManager profileManager;
    protected final EventListener eventListener;
    protected final FStore fStore;


    public ChartReportHandler(ProfileManager profileManager,
                              EventListener eventListener,
                              FStore fStore
    ) {

        this.profileManager = profileManager;
        this.eventListener = eventListener;
        this.fStore = fStore;
        this.executorService = Executors.newSingleThreadExecutor();
    }


}
