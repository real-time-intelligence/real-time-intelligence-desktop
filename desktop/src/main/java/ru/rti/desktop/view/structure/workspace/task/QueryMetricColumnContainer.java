package ru.rti.desktop.view.structure.workspace.task;

import lombok.Data;
import ru.rti.desktop.model.table.JXTableCase;

@Data
public class QueryMetricColumnContainer {

  private JXTableCase jXTableCaseQuery;
  private JXTableCase jXTableCaseMetric;
  private JXTableCase jXTableCaseColumn;

  public QueryMetricColumnContainer() {
  }

  public void addQueryToCard(JXTableCase jXTableCaseQuery) {
    this.jXTableCaseQuery = jXTableCaseQuery;
  }

  public void addMetricToCard(JXTableCase jXTableCaseMetric) {
    this.jXTableCaseMetric = jXTableCaseMetric;
  }

  public void addColumnToCard(JXTableCase jXTableCaseColumn) {
    this.jXTableCaseColumn = jXTableCaseColumn;
  }
}
