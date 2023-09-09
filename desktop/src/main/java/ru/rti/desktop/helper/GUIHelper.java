package ru.rti.desktop.helper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.cstype.CType;
import org.fbase.model.profile.cstype.SType;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.painlessgridbag.PainlessGridBag;
import org.painlessgridbag.PainlessGridbagConfiguration;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.view.chart.report.StackChartReportPanel;
import ru.rti.desktop.model.textfield.JTextFieldCase;
import ru.rti.desktop.view.panel.config.ButtonPanel;

@Log4j2
public class GUIHelper {

    public static JXTable getJXTable(int rowCount) {
        JXTable jxTable = new JXTable();
        jxTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jxTable.setColumnControlVisible(true);
        jxTable.setHorizontalScrollEnabled(true);
        jxTable.setEditable(false);
        jxTable.setVisibleRowCount(rowCount);
        jxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return jxTable;
    }

    public static JScrollPane getJScrollPane(JXTable jxTable) {
        JScrollPane jScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        jScrollPane.setViewportView(jxTable);
        jScrollPane.setVerticalScrollBar(jScrollPane.getVerticalScrollBar());

        return jScrollPane;
    }

    public static JScrollPane getTextInJScrollPane(JTextArea jTextArea) {
        JPanel other = new JPanel(new BorderLayout());
        other.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        return new JScrollPane(jTextArea);
    }

    public static JTextArea getJTextArea(int rows, int columns) {
        JTextArea jTextArea = new JTextArea(rows, columns);
        jTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
        jTextArea.setLineWrap(true);
        jTextArea.setAutoscrolls(true);
        jTextArea.setEditable(true);

        return jTextArea;
    }

    public static JXTextArea getJXTextArea(int rows, int columns) {
        JXTextArea jTextArea = new JXTextArea();
        jTextArea.setRows(rows);
        jTextArea.setColumns(columns);
        jTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
        jTextArea.setLineWrap(true);
        jTextArea.setAutoscrolls(true);
        jTextArea.setEditable(true);

        return jTextArea;
    }

    public static JButton getJButton(String text) {
        JButton button = new JButton(text);
        button.setVerticalAlignment(SwingConstants.TOP);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        return button;
    }

    public static PainlessGridbagConfiguration getPainlessGridbagConfiguration() {
        PainlessGridbagConfiguration config = new PainlessGridbagConfiguration();
        config.setFirstColumnLeftSpacing(5);
        config.setFirstRowTopSpacing(5);
        config.setLastColumnRightSpacing(5);
        config.setLastRowBottomSpacing(5);
        return config;
    }

    public static PainlessGridbagConfiguration getPainlessGridbagConfigurationNull() {
        PainlessGridbagConfiguration config = new PainlessGridbagConfiguration();
        config.setFirstColumnLeftSpacing(0);
        config.setFirstRowTopSpacing(0);
        config.setLastColumnRightSpacing(0);
        config.setLastRowBottomSpacing(0);
        return config;
    }

    public static JSplitPane getJSplitPane(int orientation, int dividerSize, int dividerLocation) {
        JSplitPane chartGanttPane = new JSplitPane();
        chartGanttPane.setOrientation(orientation);
        chartGanttPane.setOneTouchExpandable(true);
        chartGanttPane.setDividerSize(dividerSize);
        chartGanttPane.setDividerLocation(dividerLocation);

        return chartGanttPane;
    }

    public static int getIdByColumnName(JXTable jxTable, DefaultTableModel defaultTableModel,
                                        ListSelectionModel listSelectionModel, String profileColumnName) {
        int profileId = 0;
        int minIndex = listSelectionModel.getMinSelectionIndex();
        int maxIndex = listSelectionModel.getMaxSelectionIndex();
        for (int i = minIndex; i <= maxIndex; i++) {
            if (listSelectionModel.isSelectedIndex(i)) {
                profileId = (int) jxTable.getModel().getValueAt(i, defaultTableModel.findColumn(profileColumnName));
            }
        }

        return profileId;
    }

    public static String getNameByColumnName(JXTable jxTable, DefaultTableModel defaultTableModel,
                                             ListSelectionModel listSelectionModel, String profileColumnName) {
        String name = "";
        int minIndex = listSelectionModel.getMinSelectionIndex();
        int maxIndex = listSelectionModel.getMaxSelectionIndex();
        for (int i = minIndex; i <= maxIndex; i++) {
            if (listSelectionModel.isSelectedIndex(i)) {
                name = (String) jxTable.getModel().getValueAt(i, defaultTableModel.findColumn(profileColumnName));
            }
        }

        return name;
    }

    public static JCheckBox getJCheckBox(String text) {
        return new JCheckBox(text);
    }

    public static JXTableCase getJXTableCase(int rowCountTable, Object[] columnNamesModel) {
        JXTable jxTable = getJXTable(rowCountTable);
        DefaultTableModel defaultTableModel = new DefaultTableModel(columnNamesModel, 0);
        jxTable.setModel(defaultTableModel);
        jxTable.setSortable(false);

        JScrollPane jScrollPane = getJScrollPane(jxTable);

        return new JXTableCase(jxTable, defaultTableModel, jScrollPane);
    }

    public static JTextFieldCase getJTextFieldCase(String jLabelText) {
        JPanel jPanel = new JPanel();
        JLabel jLabel = new JLabel(jLabelText);
        JTextField jTextField = new JTextField();

        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row()
            .cell(jLabel).cell(jTextField).fillX();
        gbl.done();

        return new JTextFieldCase(jPanel, jLabel, jTextField);
    }

    public static JXTableCase getEditJXTableCase(int rowCountTable, Object[] columnNamesModel) {
        JXTable jxTable = getJXTable(rowCountTable);
        jxTable.setEditable(true);
        DefaultTableModel defaultTableModel = new DefaultTableModel(columnNamesModel, 0);

        jxTable.setModel(defaultTableModel);
        jxTable.setSortable(false);

        JScrollPane jScrollPane = getJScrollPane(jxTable);

        return new JXTableCase(jxTable, defaultTableModel, jScrollPane);
    }

    public static JXTableCase getJXTableCaseMetadata(Object[] columnNamesModel) {
        DefaultTableModel defaultTableModel = new DefaultTableModel(columnNamesModel, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 3;
            }
        };

        JComboBox<SType> comboSType = new JComboBox<>(SType.values());
        JComboBox<CType> comboCType = new JComboBox<>(CType.values());

        JXTable jxTable = new JXTable(defaultTableModel) {
            //  Determine editor to be used by row
            public TableCellEditor getCellEditor(int row, int column) {
                int modelColumn = convertColumnIndexToModel(column);

                if (modelColumn == 4) {
                    return new DefaultCellEditor(comboSType);
                } else if (modelColumn == 5) {
                    return new DefaultCellEditor(comboCType);
                } else {
                    return super.getCellEditor(row, column);
                }
            }
        };
        jxTable.setColumnControlVisible(true);
        jxTable.setSortable(false);

        JScrollPane jScrollPane = getJScrollPane(jxTable);

        return new JXTableCase(jxTable, defaultTableModel, jScrollPane);
    }

    public static JXTableCase getJXTableCaseCheckBox(int rowCountTable, Object[] columnNamesModel,int BOOLEAN_COL) {

        JXTable jxTable = getJXTable(rowCountTable);
        DefaultTableModel defaultTableModel = new DefaultTableModel(columnNamesModel, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == BOOLEAN_COL) {

                    return Boolean.class;
                } else {
                    return String.class;
                }
            }
        };
        jxTable.setModel(defaultTableModel);
        jxTable.setSortable(false);
        jxTable.setEditable(true);

        JScrollPane jScrollPane = getJScrollPane(jxTable);

        return new JXTableCase(jxTable, defaultTableModel, jScrollPane);
    }

    public static void addToJSplitPane(JSplitPane jSplitPane, Component component, Object constraints, int location) {
        jSplitPane.add(component, constraints);
        jSplitPane.setDividerLocation(location);
        jSplitPane.repaint();
        jSplitPane.revalidate();
    }

    public static void addToJSplitPane(JSplitPane jSplitPane, StackChartReportPanel component, Object constraints, int location) {
        jSplitPane.add(component, constraints);
        jSplitPane.setDividerLocation(location);
        jSplitPane.repaint();
        jSplitPane.revalidate();
    }


    public static void addToJXTaskPane(JXTaskPane jxTaskPane, Component component) {
        jxTaskPane.add(component);
        jxTaskPane.repaint();
        jxTaskPane.revalidate();
    }

    public static void addToJSplitPane(JSplitPane jSplitPane, Component component, Object constraints) {
        jSplitPane.add(component, constraints);
        jSplitPane.repaint();
        jSplitPane.revalidate();
    }

    public static Border getBorder() {
        Border inner = BorderFactory.createEmptyBorder(2, 6, 2, 0);
        Border outer = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);

        return BorderFactory.createCompoundBorder(outer, inner);
    }

    public static class ActiveColumnCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        }
    }

    public static void disableButton(ButtonPanel buttonPanel, Boolean isSelected) {
        buttonPanel.getBtnNew().setEnabled(isSelected);
        buttonPanel.getBtnDel().setEnabled(isSelected);
        buttonPanel.getBtnCopy().setEnabled(isSelected);
        buttonPanel.getBtnEdit().setEnabled(isSelected);
        buttonPanel.getBtnSave().setEnabled(false);
        buttonPanel.getBtnCancel().setEnabled(false);
    }

    public static JXTableCase getJXTableCaseMetrics(Object[] columnNamesModel) {
        DefaultTableModel defaultTableModel = new DefaultTableModel(columnNamesModel, 0);

        JComboBox<SType> comboSType = new JComboBox<>(SType.values());
        AutoCompleteDecorator.decorate(comboSType);
        JComboBox<CType> comboCType = new JComboBox<>(CType.values());
        AutoCompleteDecorator.decorate(comboCType);
        JXTable jxTable = new JXTable(defaultTableModel) {
            //  Determine editor to be used by row
            public TableCellEditor getCellEditor(int row, int column) {
                int modelColumn = convertColumnIndexToModel(column);

                if (modelColumn == 4) {
                    return new DefaultCellEditor(comboSType);
                } else if (modelColumn == 5) {
                    return new DefaultCellEditor(comboCType);
                } else {
                    return super.getCellEditor(row, column);
                }
            }
        };
        jxTable.setColumnControlVisible(true);
        jxTable.setSortable(false);

        JScrollPane jScrollPane = getJScrollPane(jxTable);

        return new JXTableCase(jxTable, defaultTableModel, jScrollPane);
    }
}
