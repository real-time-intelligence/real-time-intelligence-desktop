package ru.rti.desktop.view.chart.stacked;

import org.fbase.core.FStore;
import org.fbase.model.output.StackedColumn;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.view.chart.FunctionDataHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MetricFunctionDataHandler implements FunctionDataHandler {
    protected Metric metric;
    protected FStore fStore;
    protected QueryInfo queryInfo;

    public MetricFunctionDataHandler(Metric metric, QueryInfo queryInfo, FStore fStore) {
        this.metric = metric;
        this.fStore = fStore;
        this.queryInfo = queryInfo;
    }

    protected void fillSeries(List<StackedColumn> sColumnList, Set<String> series) {
        sColumnList.stream()
                .map(StackedColumn::getKeyCount)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .forEach(series::add);
    }

}
