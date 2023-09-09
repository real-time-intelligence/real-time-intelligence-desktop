package ru.rti.desktop.view.structure.navigator;

import dagger.Lazy;
import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.helper.GUIHelper.ActiveColumnCellRenderer;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.view.structure.NavigatorView;
import ru.rti.desktop.view.structure.action.ActionsContainer;

@Log4j2
@Singleton
public class NavigatorViewImpl extends JPanel implements NavigatorView {
  private final Lazy<NavigatorPresenter> navigatorPresenter;
  private final JXTableCase navigatorProfileCase;

  private final JSplitPane splitProfileListButtonsAndStatus;
  private final JPanel jPanelProfileStatus;

  private final ActionsContainer navigatorActions;

  @Inject
  public NavigatorViewImpl(@Named("navigatorPresenter") Lazy<NavigatorPresenter> navigatorPresenter,
                           @Named("navigatorProfileCase") JXTableCase navigatorProfileCase,
                           @Named("navigatorActions") ActionsContainer navigatorActions,
                           @Named("splitProfileListButtonsAndStatus") JSplitPane splitProfileListButtonsAndStatus,
                           @Named("jPanelProfileStatus") JPanel jPanelProfileStatus) {
    this.navigatorPresenter = navigatorPresenter;
    this.navigatorProfileCase = navigatorProfileCase;
    this.navigatorActions = navigatorActions;
    this.splitProfileListButtonsAndStatus = splitProfileListButtonsAndStatus;
    this.jPanelProfileStatus = jPanelProfileStatus;

    this.setLayout(new BorderLayout());
    this.setBorder(new EtchedBorder());
    this.splitProfileListButtonsAndStatus.add(this.navigatorProfileCase.getJScrollPane(), JSplitPane.TOP);
    this.splitProfileListButtonsAndStatus.add(this.jPanelProfileStatus, JSplitPane.BOTTOM);

    this.add(this.splitProfileListButtonsAndStatus, BorderLayout.CENTER);
  }

  @Override
  public void bindPresenter() {
    this.navigatorPresenter.get().fillModel();

    navigatorProfileCase.getJxTable().getColumnExt(0).setVisible(false);
    navigatorProfileCase.getJxTable().getColumnModel().getColumn(0).setCellRenderer(new ActiveColumnCellRenderer());

    navigatorProfileCase.getJxTable().getSelectionModel().addListSelectionListener(this.navigatorPresenter.get());
    navigatorProfileCase.getJxTable().addKeyListener(this.navigatorPresenter.get());
  }

}
