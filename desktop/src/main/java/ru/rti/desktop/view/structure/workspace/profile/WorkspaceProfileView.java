package ru.rti.desktop.view.structure.workspace.profile;

import dagger.Lazy;

import java.awt.*;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import lombok.extern.log4j.Log4j2;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.Application;
import ru.rti.desktop.config.prototype.profile.WorkspaceProfileComponent;
import ru.rti.desktop.config.prototype.profile.WorkspaceProfileModule;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskKey;
import ru.rti.desktop.model.RunStatus;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.ProfileStartStopListener;
import ru.rti.desktop.view.structure.workspace.WorkspacePresenter;
import ru.rti.desktop.view.structure.workspace.task.WorkspaceTaskView;

@Log4j2
public class WorkspaceProfileView extends JPanel implements ProfileStartStopListener {

    private final int profileId;

    private final JTabbedPane profileTaskPane = new JTabbedPane();

    private final WorkspaceProfileComponent workspaceProfileComponent;

    @Inject
    @Named("eventListener")
    EventListener eventListener;

    @Inject
    @Named("profileManager")
    ProfileManager profileManager;

    @Inject
    @Named("workspacePresenter")
    Lazy<WorkspacePresenter> workspacePresenter;

    @Inject
    @Named("splitProfileListButtonsAndStatus")
    JSplitPane splitProfileListAndButtons;

    @Named("jPanelProfileStatus")
    @Inject
    JPanel jPanelProfileStatus;

    @Named("workspaceProfileStartButton")
    @Inject
    JButton startButton;

    @Named("workspaceProfileStopButton")
    @Inject
    JButton stopButton;

    @Named("profileStatusJLabel")
    @Inject
    JLabel profileStatusJLabel;

    public WorkspaceProfileView(int profileId) {
        this.workspaceProfileComponent = Application.getInstance().initProfile(new WorkspaceProfileModule(this));
        this.workspaceProfileComponent.inject(this);

        this.profileId = profileId;

        this.workspacePresenter.get();

        this.startButton.addActionListener(workspacePresenter.get());
        this.stopButton.addActionListener(workspacePresenter.get());

        this.setupUIForProfileInfo();

        this.cleanStartStopButtonPanel();

        this.setProfileStatusJLabel(profileId);

        this.setLayout(new BorderLayout());

        this.add(profileTaskPane);
    }

    public void setupUIForProfileInfo() {
        ProfileInfo profileInfo = profileManager.getProfileInfoById(profileId);

        startButton.setEnabled(profileInfo.getStatus().equals(RunStatus.NOT_RUNNING));
        stopButton.setEnabled(profileInfo.getStatus().equals(RunStatus.RUNNING));

        profileInfo.getTaskInfoList().forEach(taskId -> {
            TaskInfo taskInfo = this.profileManager.getTaskInfoById(taskId);
            profileTaskPane.add(taskInfo.getName(), getTaskView(taskInfo.getId()));
        });
    }

    private void cleanStartStopButtonPanel() {
        jPanelProfileStatus.removeAll();
        jPanelProfileStatus.revalidate();
        jPanelProfileStatus.repaint();

        jPanelProfileStatus.add(getProfileInfoJPanel());

        splitProfileListAndButtons.setDividerLocation(150);
    }

    private JPanel getProfileInfoJPanel() {
        JPanel jPanel = new JPanel();
        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gbl.row()
                .cellXRemainder(getButtonPanel()).fillX();
        gbl.row()
                .cellXRemainder(getStatusPanel()).fillX();
        gbl.row()
                .cellXYRemainder(new JLabel()).fillXY();

        gbl.done();

        return jPanel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();

        buttonPanel.setBorder(new EtchedBorder());
        PainlessGridBag gblButton = new PainlessGridBag(buttonPanel, GUIHelper.getPainlessGridbagConfiguration(), false);

        gblButton.row()
                .cell(startButton)
                .cell(stopButton);

        gblButton.done();

        return buttonPanel;
    }

    private JPanel getStatusPanel() {
        JPanel buttonPanel = new JPanel();

        PainlessGridBag gblProfileStatus = new PainlessGridBag(buttonPanel, GUIHelper.getPainlessGridbagConfiguration(), false);

        gblProfileStatus.row()
                .cell(profileStatusJLabel);

        gblProfileStatus.done();

        return buttonPanel;
    }

    private JPanel getTaskView(int taskId) {
        JSplitPane sqlPane = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 155);

        ProfileTaskKey profileTaskKey = new ProfileTaskKey(profileId, taskId);
        WorkspaceTaskView workspaceTaskView = new WorkspaceTaskView(sqlPane, profileTaskKey, workspaceProfileComponent);
        workspaceTaskView.loadSql();

        PainlessGridBag gbl = new PainlessGridBag(workspaceTaskView, GUIHelper.getPainlessGridbagConfiguration(), false);

        gbl.row()
                .cellXYRemainder(sqlPane).fillXY();

        gbl.done();

        return workspaceTaskView;
    }
    
    private void setProfileStatusJLabel(int profileId) {
        ProfileInfo profileInfo = profileManager.getProfileInfoById(profileId);
        profileStatusJLabel.setText("Status: " + profileInfo.getStatus().getDescription());
    }

    @Override
    public void fireOnStartOnWorkspaceProfileView(int profileId) {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        setProfileStatusJLabel(profileId);
    }

    @Override
    public void fireOnStopOnWorkspaceProfileView(int profileId) {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        setProfileStatusJLabel(profileId);
    }
}
