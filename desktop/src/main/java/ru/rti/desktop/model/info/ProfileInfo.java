package ru.rti.desktop.model.info;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.rti.desktop.model.RunStatus;

import java.util.List;

@Data
@Accessors(chain = true)
public class ProfileInfo {

  private int id;
  private String name;
  private String description;
  private RunStatus status;
  private List<Integer> taskInfoList;
}
