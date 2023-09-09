package ru.rti.desktop.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import ru.rti.desktop.HandlerMock;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.gui.RangeInfo;


@Log4j2
public class AppCacheTest extends HandlerMock {

  @Test
  public void put_and_get_test() {
    assertNotNull(appCacheLazy.get());

    int profileId = 1;
    int taskId = 1;
    int queryId = 1;
    RangeInfo rangeInfo1 = new RangeInfo(1, 1, 2, "A");
    RangeInfo rangeInfo2 = new RangeInfo(2, 3, 4, "B");

    ProfileTaskQueryKey profileTaskQueryKey =
        new ProfileTaskQueryKey(profileId, taskId, queryId);

    appCacheLazy.get().putRangeInfo(profileTaskQueryKey, rangeInfo1);
    appCacheLazy.get().putRangeInfo(profileTaskQueryKey, rangeInfo2);

    assertEquals(2,
        appCacheLazy.get().getRangeInfo(profileTaskQueryKey).size());
  }

}
