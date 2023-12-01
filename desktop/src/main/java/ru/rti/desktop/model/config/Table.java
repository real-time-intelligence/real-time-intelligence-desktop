package ru.rti.desktop.model.config;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.fbase.model.profile.TProfile;

@EqualsAndHashCode(callSuper = true)
@Data
public class Table extends TProfile {

  List<String> valuableColumnList;
}
