package ru.rti.desktop.view.pane;

import ru.rti.desktop.model.view.EditTabbedPane;

import javax.swing.*;

public class JTabbedPaneConfig extends JTabbedPane {

  public void setSelectedTab(EditTabbedPane tabbedPane) {
    switch (tabbedPane) {
      case PROFILE: {this.setSelectedIndex(0); return;}
      case TASK: {this.setSelectedIndex(1); return;}
      case CONNECTION: {this.setSelectedIndex(2); return;}
      case QUERY: {this.setSelectedIndex(3); return;}
    }

  }
}
