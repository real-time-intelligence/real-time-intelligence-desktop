package ru.rti.desktop.view.detail;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.profile.CProfile;
import org.fbase.sql.BatchResultSet;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXFindBar;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.search.AbstractSearchable;
import org.jdesktop.swingx.search.SearchFactory;
import ru.rti.desktop.config.prototype.detail.WorkspaceRawModule;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.detail.searchable.DecoratorFactory;
import ru.rti.desktop.view.detail.searchable.MatchingTextHighlighter;
import ru.rti.desktop.view.detail.searchable.XMatchingTextHighlighter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class RawDataReportPanel extends JPanel {

    private final QueryInfo queryInfo;
    private final TableInfo tableInfo;
    private final CProfile cProfile;

    private final JXTable table;
    private final JXFindBar findBar;
    private final ResultSetRawDataJPanel resultSetRawDataJPanel;
    private final DefaultTableModel tableModel;

    private final boolean useFetchSize;
    private int fetchSize = 1000;

    protected JLabel jLabelRowCount;

    private int rowCount = 0;

    private boolean hasData = false;

    private BatchResultSet batchResultSet;

    private long begin;
    private long end;

    private final FStore fStore;

    public RawDataReportPanel(FStore fStore, QueryInfo queryInfo, TableInfo tableInfo,
                              CProfile cProfile, long begin, long end, boolean useFetchSize) {

        this.queryInfo = queryInfo;
        this.tableInfo = tableInfo;
        this.cProfile = cProfile;

        this.begin = begin;
        this.end = end;

        this.fStore = fStore;

        this.setLayout(new BorderLayout());

        this.useFetchSize = useFetchSize;

        this.jLabelRowCount = new JLabel("Rows: " + rowCount);

        this.resultSetRawDataJPanel = new ResultSetRawDataJPanel(jLabelRowCount);

        if (useFetchSize) {
            batchResultSet = fStore.getBatchResultSet(queryInfo.getName(), begin, end, fetchSize);
        }

        this.tableModel = new DefaultTableModel(getColumnHeaders(), 0);
        this.loadRawData(queryInfo.getName(), begin, end);

        this.table = new JXTable(tableModel);

        this.table.setColumnControlVisible(true);
        this.table.setHorizontalScrollEnabled(true);
        this.table.packAll();

        this.findBar = SearchFactory.getInstance().createFindBar();

        MatchingTextHighlighter matchingTextMarker = new XMatchingTextHighlighter();
        matchingTextMarker.setPainter(DecoratorFactory.createPlainPainter());
        ((AbstractSearchable) this.table.getSearchable()).setMatchHighlighter(matchingTextMarker);

        JScrollPane tableRawDataPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableRawDataPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        tableRawDataPane.setViewportView(this.table);
        tableRawDataPane.setVerticalScrollBar(tableRawDataPane.getVerticalScrollBar());

        JPanel searchablePanel = new JPanel(new BorderLayout());
        final JXCollapsiblePane collapsible = connectCollapsibleFindBarWithTable();

        searchablePanel.add(collapsible, BorderLayout.NORTH);
        searchablePanel.add(tableRawDataPane);
        searchablePanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Selected Items: "), new EmptyBorder(4, 4, 4, 4)));

        this.add(searchablePanel, BorderLayout.CENTER);
    }

    private void loadRawData(String tableName, long begin, long end) {
        log.info("Parameters begin: {}, end: {}", getDate(begin), getDate(end));

        List<List<Object>> rawData;
        if (useFetchSize) {
            rawData = batchResultSet.getObject();
        } else {
            rawData = fStore.getRawDataAll(tableName, begin, end);
        }

        if (!rawData.isEmpty()) hasData = true;

        loadToModel(rawData);
    }

    private void loadToModel(List<List<Object>> rawData) {
        List<CProfile> timeStampIndex = tableInfo.getCProfiles()
                .stream()
                .filter(f -> (f.getCsType().isTimeStamp() || f.getColDbTypeName().contains("TIMESTAMP")))
                .toList();

        if (rawData == null || rawData.isEmpty()) {
            if (useFetchSize) {
                if (hasData) {
                    JOptionPane.showMessageDialog(this,
                            "No raw data found", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                log.warn("No raw data found");
            }
        } else {
            rowCount = rowCount + rawData.size();
            resultSetRawDataJPanel.updateJLabelRowCount(rowCount);

            rawData.forEach(row -> {
                Object[] rawObj = row.toArray();

                timeStampIndex.forEach(cProfile -> {
                    if (cProfile.getCsType().isTimeStamp()) {
                        rawObj[cProfile.getColId()] = getDate((Long) rawObj[cProfile.getColId()]);
                    }
                });

                tableModel.addRow(rawObj);
            });
        }
    }

    private String getDate(long l) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date date = new Date(l);
        return dateFormat.format(date);
    }

    private String[] getColumnHeaders() {
        String[] columnHeaders = new String[tableInfo.getCProfiles().size()];
        AtomicInteger at = new AtomicInteger(0);

        tableInfo.getCProfiles().forEach(e -> columnHeaders[at.getAndIncrement()] = e.getColName());

        return columnHeaders;
    }

    private JXCollapsiblePane connectCollapsibleFindBarWithTable() {
        if (useFetchSize) {
            this.findBar.add(resultSetRawDataJPanel);
        } else {
            this.findBar.add(jLabelRowCount);
        }

        final JXCollapsiblePane collapsible = new JXCollapsiblePane();
        this.table.putClientProperty(AbstractSearchable.MATCH_HIGHLIGHTER, Boolean.TRUE);
        this.findBar.setSearchable(this.table.getSearchable());
        collapsible.add(this.findBar);
        collapsible.setCollapsed(false);

        Action openFindBar = new AbstractActionExt() {
            public void actionPerformed(ActionEvent e) {
                collapsible.setCollapsed(false);
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(findBar);
            }
        };
        Action closeFindBar = new AbstractActionExt() {
            public void actionPerformed(ActionEvent e) {
                collapsible.setCollapsed(true);
                table.requestFocusInWindow();
            }
        };

        this.table.getActionMap().put("find", openFindBar);
        this.findBar.getActionMap().put("close", closeFindBar);

        return collapsible;
    }

    private class CustomRenderer extends DefaultTableCellRenderer {

        private final DefaultTableModel model;

        public CustomRenderer(DefaultTableModel model) {
            this.model = model;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);

            if (cProfile != null &&
                    model.getColumnName(table.convertColumnIndexToModel(col)).equals(cProfile.getColName())) {
                c.setBackground(Color.lightGray);
            }

            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
            } else {
                c.setBackground(table.getBackground());
            }
            return c;
        }
    }

    class ResultSetRawDataJPanel extends JXPanel {
        protected JLabel jLabelRowCount;
        protected JButton findNext;

        public ResultSetRawDataJPanel(JLabel jLabelRowCount) {
            this.jLabelRowCount = jLabelRowCount;

            this.setLayout(new FlowLayout(10));

            this.findNext = new JButton("Next " + fetchSize + " rows");

            this.findNext.addActionListener(e -> {
                if (e.getSource() == this.findNext) {
                    log.info("Fetch next batch of " + fetchSize + " rows..");
                    loadToModel(batchResultSet.getObject());
                }
            });

            this.add(this.jLabelRowCount);
            this.add(this.findNext);
        }

        public void updateJLabelRowCount(int rowCount) {
            jLabelRowCount.setText("Rows: " + rowCount);
            jLabelRowCount.paintImmediately(jLabelRowCount.getVisibleRect());
        }
    }

}
