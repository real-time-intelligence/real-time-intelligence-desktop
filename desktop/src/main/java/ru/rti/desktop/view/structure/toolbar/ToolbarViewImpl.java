package ru.rti.desktop.view.structure.toolbar;

import dagger.Lazy;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.view.ToolbarButtonState;
import ru.rti.desktop.view.structure.ToolbarView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

@Log4j2
@Singleton
public class ToolbarViewImpl extends JToolBar implements ToolbarView {

    private final Lazy<ToolbarPresenter> toolbarPresenter;
    private final JButton toolbarConfigButton;
    private final JButton toolbarTemplateButton;
    private final JButton toolbarReportButton;

    @Inject
    public ToolbarViewImpl(@Named("toolbarPresenter") Lazy<ToolbarPresenter> toolbarPresenter,
                           @Named("toolbarConfigButton") JButton toolbarConfigButton,
                           @Named("toolbarTemplateButton") JButton toolbarTemplateButton,
                           @Named("toolbarReportButton") JButton toolbarReportButton) {
        this.toolbarPresenter = toolbarPresenter;
        this.toolbarConfigButton = toolbarConfigButton;
        this.toolbarTemplateButton = toolbarTemplateButton;
        this.toolbarReportButton = toolbarReportButton;

        this.setBorder(new EtchedBorder());

        this.add(Box.createRigidArea(new Dimension(10, 0)));
        this.add(this.toolbarConfigButton);
        this.add(Box.createRigidArea(new Dimension(10, 0)));
        this.add(this.toolbarTemplateButton);

        this.add(Box.createRigidArea(new Dimension(10, 0)));
        this.add(this.toolbarReportButton);

    }

    @Override
    public void bindPresenter() {
        this.toolbarConfigButton.addActionListener(this.toolbarPresenter.get());
        this.toolbarTemplateButton.addActionListener(this.toolbarPresenter.get());
        this.toolbarReportButton.addActionListener(this.toolbarPresenter.get());
    }

    @Override
    public void setProfileButtonState(ToolbarButtonState toolbarButtonState) {
        this.toolbarConfigButton.setEnabled(toolbarButtonState == ToolbarButtonState.ENABLE);
        this.toolbarTemplateButton.setEnabled(toolbarButtonState == ToolbarButtonState.ENABLE);
        this.toolbarReportButton.setEnabled(toolbarButtonState == ToolbarButtonState.ENABLE);
    }

}
