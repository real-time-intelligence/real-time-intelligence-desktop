package ru.rti.desktop.model.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Connection extends ConfigEntity {
  private String userName;
  private String password;
  private String url;
  private String jar;
  private String driver;
}
