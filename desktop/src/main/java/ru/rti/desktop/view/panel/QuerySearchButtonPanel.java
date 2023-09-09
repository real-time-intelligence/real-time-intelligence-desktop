package ru.rti.desktop.view.panel;

import lombok.Data;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

@Data
public class QuerySearchButtonPanel extends JPanel {
    private final JLabel lblSearch;
    private final JTextField jTextFieldSearch;
    private final JButton jButtonSearch;

    public QuerySearchButtonPanel() {
        this.lblSearch = new JLabel("Search");
        this.jTextFieldSearch = new JTextField(20);
        this.jButtonSearch = new JButton("Go");

        PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gbl.row()
                .cell(getButtonPanel())
                .fillX();

        gbl.done();
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();

        buttonPanel.setBorder(new EtchedBorder());
        PainlessGridBag gblButton = new PainlessGridBag(buttonPanel, GUIHelper.getPainlessGridbagConfiguration(), false);

        gblButton.row()
                .cell(lblSearch)
                .cell(jTextFieldSearch)
                .cell(jButtonSearch)
                .separator();

        gblButton.done();

        return buttonPanel;
    }
}
