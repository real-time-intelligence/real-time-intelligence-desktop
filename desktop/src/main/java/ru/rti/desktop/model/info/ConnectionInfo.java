package ru.rti.desktop.model.info;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.rti.desktop.model.db.DBType;

@Data
@Accessors(chain = true)
public class ConnectionInfo {
  private int id;
  private String name;
  private String userName;
  private String password;
  private String url;
  private String jar;
  private String driver;
  private DBType dbType;
}
