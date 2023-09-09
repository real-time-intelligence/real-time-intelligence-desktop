package ru.rti.desktop.manager;

import java.util.List;
import ru.rti.desktop.model.config.Query;

public interface TemplateManager {

  <T> List<T> getConfigList(Class<T> clazz);

  List<Query> getQueryListByConnDriver(String connDriver);
}
