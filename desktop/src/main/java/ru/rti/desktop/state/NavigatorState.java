package ru.rti.desktop.state;

public interface NavigatorState {

  void setSelectionIndex(int selectionIndex);

  int getSelectionIndex();

  void setSelectedProfile(int profileId);

  int getSelectedProfile();
}
