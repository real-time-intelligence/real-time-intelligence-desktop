package ru.rti.desktop.collector.loader;

public interface DataLoader {

  void initLastTimestamp();

  void loadDataDirect();

  void loadDataJdbc();
}
