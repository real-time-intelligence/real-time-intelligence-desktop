package ru.rti.desktop.config.view;

import dagger.Module;
import dagger.Provides;
import java.awt.event.KeyEvent;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.KeyStroke;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.column.ProfileColumnNames;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.view.structure.action.ActionsContainer;

@Module
public class NavigatorConfig {

  @Provides
  @Singleton
  @Named("navigatorProfileCase")
  public JXTableCase getTemplateConnCase() {
    JXTableCase jxTableCase = GUIHelper.getJXTableCase(100,
        new String[]{ProfileColumnNames.ID.getColName(), "Profile name",});
    jxTableCase.getJxTable().getTableHeader().setVisible(true);
    jxTableCase.getJxTable().setSortable(false);
    return jxTableCase;
  }

  @Provides
  @Singleton
  @Named("navigatorActions")
  public ActionsContainer getNavigatorActions() {
    ActionsContainer actionsContainer = new ActionsContainer();
    actionsContainer.getKeyStrokeMap().put("up", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
    actionsContainer.getKeyStrokeMap().put("down", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));

    return actionsContainer;
  }
}
