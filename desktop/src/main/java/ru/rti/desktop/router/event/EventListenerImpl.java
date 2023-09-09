package ru.rti.desktop.router.event;

import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.CProfile;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.view.*;
import ru.rti.desktop.router.listener.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Singleton
public class EventListenerImpl implements EventListener {
    private List<ToolbarListener> profileButtonStateListenerList = new ArrayList<>();
    private List<ToolbarListener> profileSelectOnNavigatorListenerList = new ArrayList<>();
    private List<ConfigListener> configListenerList = new ArrayList<>();
    private List<TemplateListener> templateListenerList = new ArrayList<>();
    private List<ReportListener> reportListenerList = new ArrayList<>();
    private List<ProgressbarListener> progressbarListenerList = new ArrayList<>();
    private List<WorkspaceListener> workspaceListenerList = new ArrayList<>();
    private List<ProfileStartStopListener> profileStartStopListenerList = new ArrayList<>();
    private Map<ProfileTaskQueryKey, CollectStartStopListener> collectStartStopListenerMap = new ConcurrentHashMap<>();
    private List<ShowLocalHistoryListener> showLocalHistoryListenerList = new ArrayList<>();
    private List<AppCacheAddListener> appCacheAddListenerList = new ArrayList<>();
    private List<ProfileAddListener> profileAddListeners = new ArrayList<>();

    @Inject
    public EventListenerImpl() {
    }

    @Override
    public void fireToolbarButtonStateChange(ToolbarButtonState toolbarButtonState) {
        profileButtonStateListenerList.forEach(l -> l.fireToolbarButtonStateChange(toolbarButtonState));
    }

    @Override
    public void fireOnSelectProfileOnNavigator(int profileId) {
        workspaceListenerList.forEach(l -> l.fireOnSelectProfileOnNavigator(profileId));
    }

    @Override
    public void addProfileButtonStateListener(ToolbarListener toolbarListener) {
        profileButtonStateListenerList.add(toolbarListener);
    }

    @Override
    public void addProfileSelectOnNavigator(ToolbarListener toolbarListener) {
        profileSelectOnNavigatorListenerList.add(toolbarListener);
    }

    @Override
    public void addConfigStateListener(ConfigListener configListener) {
        configListenerList.add(configListener);
    }

    @Override
    public void fireShowConfig(ConfigState configState) {
        configListenerList.forEach(l -> l.fireShowConfig(configState));
    }

    @Override
    public void addTemplateStateListener(TemplateListener configListener) {
        templateListenerList.add(configListener);
    }

    @Override
    public void fireShowTemplate(TemplateState templateState) {
        templateListenerList.forEach(l -> l.fireShowTemplate(templateState));
    }

    @Override
    public void addProgressbarListener(ProgressbarListener progressbarListener) {
        progressbarListenerList.add(progressbarListener);
    }

    @Override
    public void addProfileStartStopListener(ProfileStartStopListener profileStartStopListener) {
        profileStartStopListenerList.add(profileStartStopListener);
    }

    @Override
    public void addCollectStartStopListener(ProfileTaskQueryKey profileTaskQueryKey, CollectStartStopListener collectStartStopListener) {
        collectStartStopListenerMap.put(profileTaskQueryKey, collectStartStopListener);
    }

    @Override
    public void addShowLocalHistoryListener(ShowLocalHistoryListener showLocalHistoryListener) {
        showLocalHistoryListenerList.add(showLocalHistoryListener);
    }

    @Override
    public void addAppCacheAddListener(AppCacheAddListener appCacheAddListener) {
        appCacheAddListenerList.add(appCacheAddListener);
    }

    @Override
    public void addProfileAddListener(ProfileAddListener profileAddListener) {
        profileAddListeners.add(profileAddListener);
    }

    @Override
    public <T> void clearListener(Class<T> genericClass) {
        profileStartStopListenerList.removeIf(genericClass::isInstance);
        collectStartStopListenerMap.entrySet().removeIf(map -> genericClass.isInstance(map.getValue()));
        showLocalHistoryListenerList.removeIf(genericClass::isInstance);
        appCacheAddListenerList.removeIf(genericClass::isInstance);
    }

    @Override
    public void clearListenerByKey(ProfileTaskQueryKey profileTaskQueryKey) {
        collectStartStopListenerMap.entrySet().removeIf(map -> profileTaskQueryKey.equals(map.getKey()));
    }

    @Override
    public void addProfileSelectOnNavigator(WorkspaceListener workspaceListener) {
        workspaceListenerList.add(workspaceListener);
    }

    @Override
    public void fireProgressbarVisible(ProgressbarState progressbarState) {
        progressbarListenerList.forEach(l -> l.fireProgressbarVisible(progressbarState));
    }

    @Override
    public void fireOnStartOnWorkspaceProfileView(int profileId) {
        profileStartStopListenerList.forEach(l -> l.fireOnStartOnWorkspaceProfileView(profileId));
    }

    @Override
    public void fireOnStopOnWorkspaceProfileView(int profileId) {
        profileStartStopListenerList.forEach(l -> l.fireOnStopOnWorkspaceProfileView(profileId));
    }

    @Override
    public void fireOnStartCollect(ProfileTaskQueryKey profileTaskQueryKey) {
        collectStartStopListenerMap.entrySet().stream()
                .filter(f -> f.getKey().equals(profileTaskQueryKey))
                .forEach(l -> l.getValue().fireOnStartCollect(profileTaskQueryKey));
    }

    @Override
    public void fireOnStopCollect(ProfileTaskQueryKey profileTaskQueryKey) {
        collectStartStopListenerMap.entrySet().stream()
                .filter(f -> f.getKey().equals(profileTaskQueryKey))
                .forEach(l -> l.getValue().fireOnStopCollect(profileTaskQueryKey));
    }

    @Override
    public void fireOnShowHistory(QueryInfo queryInfo, CProfile cProfile, long begin, long end) {
        showLocalHistoryListenerList.forEach(l -> l.fireOnShowHistory(queryInfo, cProfile, begin, end));
    }

    @Override
    public void fireOnAddToAppCache(ProfileTaskQueryKey profileTaskQueryKey) {
        appCacheAddListenerList.forEach(l -> l.fireOnAddToAppCache(profileTaskQueryKey));
    }

    @Override
    public void fireProfileAdd() {
        profileAddListeners.forEach(ProfileAddListener::fireProfileAdd);
    }

    @Override
    public void fireShowReport(ReportState reportState) {
        reportListenerList.forEach(l -> l.fireShowReport(reportState));
    }

    @Override
    public void addReportStateListener(ReportListener reportListener) {
        reportListenerList.add(reportListener);
    }
}
