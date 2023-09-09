package ru.rti.desktop.model.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EntityList<T> {
  private List<T> entityList;
}
