package ru.rti.desktop.model.info;

import java.util.HashMap;
import java.util.Map;
import org.fbase.model.profile.SProfile;
import org.fbase.model.profile.TProfile;
import org.fbase.model.profile.cstype.CSType;

public class TableInfo extends TProfile {

  public SProfile getSProfile() {
    SProfile sProfile = new SProfile();
    sProfile.setTableName(this.getTableName());
    sProfile.setTableType(this.getTableType());
    sProfile.setIndexType(this.getIndexType());
    sProfile.setCompression(this.getCompression());

    Map<String, CSType> map = new HashMap<>();

    if (this.getCProfiles() != null) {
      this.getCProfiles().forEach(e -> {
        map.put(e.getColName(), e.getCsType());
      });
    }

    sProfile.setCsTypeMap(map);

    return sProfile;
  }
}
