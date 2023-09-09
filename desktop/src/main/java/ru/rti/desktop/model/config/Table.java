package ru.rti.desktop.model.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.fbase.model.profile.SProfile;
import org.fbase.model.profile.TProfile;
import org.fbase.model.profile.cstype.CSType;

@EqualsAndHashCode(callSuper = true)
@Data
public class Table extends TProfile {
  public SProfile getSProfile() {
    SProfile sProfile = new SProfile();
    sProfile.setTableName(this.getTableName());

    Map<String, CSType> map = new HashMap<>();

    if (this.getCProfiles() != null) {
      this.getCProfiles().forEach(e -> map.put(e.getColName(), e.getCsType()));
    }

    sProfile.setCsTypeMap(map);

    return sProfile;
  }
}
