package ru.rti.desktop.view.panel.report;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTitledSeparator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.ProfileTaskQueryKey;

@Data
public class JXTaskPaneForChartCard extends JXTaskPane {
    @EqualsAndHashCode.Include
    private final ProfileTaskQueryKey key;
    private final JXLabel jlDetails;
    private final JXTextArea jtaDescription;
    private final JSplitPane jSplitPane;

    public JXTaskPaneForChartCard(ProfileTaskQueryKey key) {
        this.key = key;

        this.jlDetails = new JXLabel("Description");
        this.jtaDescription = GUIHelper.getJXTextArea(2, 1);
        this.jtaDescription.setPrompt("Enter a comment...");

        this.jSplitPane = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 250);
        jSplitPane.setBottomComponent(new JPanel());
        jSplitPane.setTopComponent(new JPanel());

        JPanel jPanelDesc = new JPanel();
        jPanelDesc.setBorder(new EtchedBorder());

        PainlessGridBag gblChart = new PainlessGridBag(jPanelDesc, GUIHelper.getPainlessGridbagConfiguration(), false);

        gblChart.row()
            .cell(new JXTitledSeparator("Description")).fillX();
        gblChart.row()
            .cell(new JScrollPane(jtaDescription)).fillX();

        gblChart.done();

        this.add(jPanelDesc, BorderLayout.NORTH);
        this.add(jSplitPane, BorderLayout.CENTER);
    }
}
