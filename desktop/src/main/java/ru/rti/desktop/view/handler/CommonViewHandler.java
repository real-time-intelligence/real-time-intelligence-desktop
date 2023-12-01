package ru.rti.desktop.view.handler;

import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.table.JXTableCase;

public interface CommonViewHandler {

  default void fillConfigMetadata(TableInfo tableInfo, JXTableCase configMetadataCase) {
    Object[][] rowData = tableInfo.getCProfiles().stream()
        .filter(f -> !f.getCsType().isTimeStamp())
        .map(cProfile -> {
          boolean valuableVal = tableInfo.getValuableColumnList() != null &&
              tableInfo.getValuableColumnList().stream().anyMatch(f -> f.equalsIgnoreCase(cProfile.getColName()));

          return new Object[] {
              cProfile.getColId(),
              cProfile.getColIdSql(),
              cProfile.getColName(),
              cProfile.getColDbTypeName(),
              cProfile.getCsType().getSType(),
              cProfile.getCsType().getCType(),
              valuableVal
          };
        })
        .toArray(Object[][]::new);

    configMetadataCase.getDefaultTableModel().setRowCount(0);

    for (Object[] row : rowData) {
      configMetadataCase.getDefaultTableModel().addRow(row);
    }
  }
}
