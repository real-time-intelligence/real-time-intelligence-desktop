package ru.rti.desktop.model.info;

import lombok.Data;
import ru.rti.desktop.model.config.*;

@Data
public class ConfigInfo {
  EntityList<Profile> profiles;
  EntityList<Task> tasks;
  EntityList<Connection> connections;
  EntityList<Query> queries;
}
