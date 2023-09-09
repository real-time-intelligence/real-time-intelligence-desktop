package ru.rti.desktop;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.config.DaggerMainComponent;
import ru.rti.desktop.config.MainComponent;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.utility.TestData;
import ru.rti.desktop.utility.TestData;
import ru.rti.desktop.view.BaseFrame;

@Log4j2
public class Application {
  private static MainComponent mainComponent;
  public static MainComponent getInstance() {return mainComponent;}

  /**
   * To enable test mode run application with VM option "testMode" in command line
   *
   * Example:
   *  java -DtestMode=true -Dfile.encoding=UTF8 -jar desktop-1.0-SNAPSHOT-jar-with-dependencies.jar
   * @param args
   */
  public static void main(String... args) {
    System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");

    if ("true".equals(System.getProperty("testMode"))) {
      TestData.setTestMode(true);
    }

    if ("ru".equals(System.getProperty("user.language"))) {
      Internationalization.setLanguage("ru");
    } else if  ("en".equals(System.getProperty("user.language"))) {
      Internationalization.setLanguage("en");
    } else {
      Internationalization.setLanguage();
    }

    mainComponent = ru.rti.desktop.config.DaggerMainComponent.create();

    BaseFrame baseFrame = mainComponent.createBaseFrame();
    baseFrame.setVisible(true);
  }
}
