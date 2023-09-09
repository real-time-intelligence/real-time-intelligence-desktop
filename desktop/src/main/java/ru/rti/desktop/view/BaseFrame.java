package ru.rti.desktop.view;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import ru.rti.desktop.executor.TaskExecutorPool;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskKey;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.view.structure.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Log4j2
@Singleton
public class BaseFrame extends JFrame {

    private final JSplitPane jSplitPane;

    private final NavigatorView navigatorView;
    private final WorkspaceView workspaceView;
    private final ToolbarView toolbarView;
    private final ConfigView configView;
    private final TemplateView templateView;
    private final ReportView reportView;
    private final ProgressbarView progressbarView;

    private final ProfileManager profileManager;
    private final TaskExecutorPool taskExecutorPool;
    private final FStore fStore;

    @Override
    public void remove(Component comp) {
        super.remove(comp);
    }

    @Inject
    public BaseFrame(@Named("splitBaseFrame") JSplitPane jSplitPane,
                     @Named("navigatorView") NavigatorView navigatorView,
                     @Named("workspaceView") WorkspaceView workspaceView,
                     @Named("toolbarView") ToolbarView toolbarView,
                     @Named("configView") ConfigView configView,
                     @Named("templateView") TemplateView templateView,
                     @Named("reportView") ReportView reportView,
                     @Named("progressbarView") ProgressbarView progressbarView,
                     @Named("profileManager") ProfileManager profileManager,
                     @Named("taskExecutorPool") TaskExecutorPool taskExecutorPool,
                     @Named("localDB") FStore fStore) throws HeadlessException {
        this.jSplitPane = jSplitPane;
        this.navigatorView = navigatorView;
        this.navigatorView.bindPresenter();

        this.workspaceView = workspaceView;
        this.workspaceView.bindPresenter();

        this.toolbarView = toolbarView;
        this.toolbarView.bindPresenter();

        this.configView = configView;
        this.configView.bindPresenter();

        this.templateView = templateView;
        this.templateView.bindPresenter();

        this.reportView = reportView;
        this.reportView.bindPresenter();

        this.progressbarView = progressbarView;
        this.progressbarView.bindPresenter();

        this.profileManager = profileManager;
        this.taskExecutorPool = taskExecutorPool;

        this.fStore = fStore;

        this.setTitle("Real-time-intelligence-desktop");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(400, 300));
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); //todo window max size
        this.setVisible(true);

        this.fillJSplitPane((Container) navigatorView, JSplitPane.LEFT);
        this.fillJSplitPane((Container) workspaceView, JSplitPane.RIGHT);

        this.addProfileArea((Container) this.toolbarView, BorderLayout.NORTH);
        this.addProfileArea(this.jSplitPane, BorderLayout.CENTER);

        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        queue.push(new EventQueueProxy());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                profileManager.getProfileInfoList()
                        .forEach(profileInfo -> profileInfo.getTaskInfoList().forEach(taskId -> {
                            TaskInfo taskInfo = profileManager.getTaskInfoById(taskId);

                            log.info("Close task {}", taskInfo.getName());

                            taskExecutorPool.removeTaskExecutor(new ProfileTaskKey(profileInfo.getId(), taskInfo.getId()));
                        }));
                fStore.syncBackendDb();
                fStore.closeBackendDb();

                System.exit(0);
            }
        });
    }

    public void addProfileArea(Container container, String constraints) {
        this.add(container, constraints);
    }

    public void fillJSplitPane(Container container, String constraints) {
        this.jSplitPane.add(container, constraints);
    }

    public void repaintJSplitPane() {
        this.jSplitPane.revalidate();
        this.jSplitPane.repaint();
    }

    static class EventQueueProxy extends EventQueue {

        protected void dispatchEvent(AWTEvent newEvent) {
            try {
                super.dispatchEvent(newEvent);
            } catch (Throwable t) {
                log.catching(t);
                String message = t.getMessage();

                if (message == null || message.length() == 0) {
                    message = "Fatal: " + t.getClass();
                }

                JOptionPane.showMessageDialog(null, message,
                        "General Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
