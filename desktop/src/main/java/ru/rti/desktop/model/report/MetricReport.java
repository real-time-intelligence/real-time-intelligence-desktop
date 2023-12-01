package ru.rti.desktop.model.report;

import lombok.*;
import ru.rti.desktop.model.config.Metric;

@EqualsAndHashCode(callSuper = true)
@Data
public class MetricReport extends Metric {

    private String comment;
}
