package ru.rti.desktop.view.panel;

import java.awt.GridLayout;
import javax.swing.JPanel;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.table.JXTableCase;

@Log4j2
public class TimeRangeRecentPanel extends JPanel {

    private final JXTableCase jXTableRecent;

    public TimeRangeRecentPanel(JXTableCase jXTableRecent) {
        this.jXTableRecent = jXTableRecent;
        this.setLayout(new GridLayout(1, 1));
        this.add(this.jXTableRecent.getJScrollPane());
    }
}
