package ru.rti.desktop.model;

import lombok.*;
import lombok.EqualsAndHashCode.CacheStrategy;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@ToString
@NoArgsConstructor
public class ProfileTaskQueryKey {

  private int profileId;
  private int taskId;
  private int queryId;
}
