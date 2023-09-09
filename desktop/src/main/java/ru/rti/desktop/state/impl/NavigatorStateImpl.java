package ru.rti.desktop.state.impl;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.state.NavigatorState;

import javax.inject.Inject;
import javax.inject.Singleton;

@Log4j2
@Singleton
public class NavigatorStateImpl implements NavigatorState {

  private volatile int selectionIndex;
  private volatile int profileId;

  @Inject
  public NavigatorStateImpl() {}

  @Override
  public void setSelectionIndex(int selectionIndex) {
    this.selectionIndex = selectionIndex;
  }

  @Override
  public int getSelectionIndex() {
    return selectionIndex;
  }

  @Override
  public void setSelectedProfile(int profileId) {
    this.profileId = profileId;
  }

  @Override
  public int getSelectedProfile() {
    return profileId;
  }

}
