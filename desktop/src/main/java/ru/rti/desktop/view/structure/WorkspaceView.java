package ru.rti.desktop.view.structure;

import ru.rti.desktop.view.structure.workspace.profile.WorkspaceProfileView;

public interface WorkspaceView {

    WorkspaceProfileView addWorkspaceProfileView(int profileId);

    void bindPresenter();
}
