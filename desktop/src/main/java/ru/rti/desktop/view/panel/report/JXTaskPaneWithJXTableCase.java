package ru.rti.desktop.view.panel.report;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXTaskPane;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.column.MetricsColumnNames;
import ru.rti.desktop.model.table.JXTableCase;

@Data
public class JXTaskPaneWithJXTableCase extends JXTaskPane {
    @EqualsAndHashCode.Include
    private final ProfileTaskQueryKey key;
    private final JXTableCase jtcMetric;
    private final JXTableCase jtcColumn;


    public JXTaskPaneWithJXTableCase(ProfileTaskQueryKey key) {

        this.key = key;

        jtcMetric = GUIHelper.getJXTableCase(3,
                new String[]{MetricsColumnNames.ID.getColName(), "Metrics",});
        jtcMetric.getJxTable().getColumnExt(0).setVisible(false);

        jtcColumn = GUIHelper.getJXTableCase(3,
                new String[]{MetricsColumnNames.ID.getColName(), "Columns",});
        jtcColumn.getJxTable().getColumnExt(0).setVisible(false);

        this.add(jtcMetric.getJScrollPane());
        this.add(jtcColumn.getJScrollPane());
    }
}
