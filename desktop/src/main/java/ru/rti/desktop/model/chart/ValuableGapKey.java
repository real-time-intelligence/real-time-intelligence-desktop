package ru.rti.desktop.model.chart;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@ToString
public class ValuableGapKey {

  private long begin;
  private long end;
}
