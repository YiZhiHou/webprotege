package edu.stanford.bmir.protege.web.client.projectmanager;

import edu.stanford.bmir.protege.web.shared.project.ProjectId;

/**
 * 添加同步接口定义
 */
public interface SyncProjectRequestHandler {

    /**
     * Handle a request to sync the specified project.  The project is identified by its {@link ProjectId}.
     * @param projectId The {@link ProjectId} that identifies the project to be downloaded.
     */
    void handleProjectSyncRequest(ProjectId projectId);
}
