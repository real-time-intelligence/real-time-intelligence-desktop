package ru.rti.desktop.model.chart;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsIsValueTyped {
  private Long timestamp;
  private Double value;
}
