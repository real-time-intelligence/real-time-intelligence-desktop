package ru.rti.desktop.model.chart;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsIsValue {
  private Object timestamp;
  private Object value;
}
