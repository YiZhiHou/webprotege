package edu.stanford.bmir.protege.web.server.sync;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import edu.stanford.bmir.protege.web.server.download.DownloadFormat;
import edu.stanford.bmir.protege.web.server.download.ProjectDownloader;
import edu.stanford.bmir.protege.web.server.download.ProjectDownloaderFactory;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.server.revision.RevisionManager;
import edu.stanford.bmir.protege.web.server.util.MemoryMonitor;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Apr 2017
 */
class CreateSyncTask implements Callable<Void> {

    private static final Logger logger = LoggerFactory.getLogger(CreateSyncTask.class);

    @Nonnull
    private final ProjectManager projectManager;

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final UserId userId;

    @Nonnull
    private final String projectDisplayName;

    @Nonnull
    private final RevisionNumber revisionNumber;

    @Nonnull
    private final DownloadFormat format;

    @Nonnull
    private final ProjectSyncerFactory projectSyncerFactory;

    @AutoFactory
    public CreateSyncTask(
            @Provided @Nonnull ProjectManager projectManager,
            @Nonnull ProjectId projectId,
            @Nonnull UserId userId,
            @Nonnull String projectDisplayName,
            @Nonnull RevisionNumber revisionNumber,
            @Nonnull DownloadFormat format,
            @Provided @Nonnull ProjectSyncerFactory projectSyncerFactory
    ) {
        this.projectManager = projectManager;
        this.projectId = projectId;
        this.userId = userId;
        this.projectDisplayName = projectDisplayName;
        this.revisionNumber = revisionNumber;
        this.format = format;
        this.projectSyncerFactory = projectSyncerFactory;
    }

    @Override
    public Void call() throws Exception {
        logger.info("{} {} Processing sync request", projectId, userId);
        logger.info("{} {} Creating project sync", projectId, userId);
        MemoryMonitor memoryMonitor = new MemoryMonitor(logger);
        memoryMonitor.monitorMemoryUsage();
        RevisionManager revisionManager = projectManager.getRevisionManager(projectId);
        memoryMonitor.monitorMemoryUsage();
        ProjectSyncer syncer = projectSyncerFactory.create(projectId,
                projectDisplayName,
                revisionNumber,
                format,
                revisionManager);
        syncer.writeProject();
        memoryMonitor.monitorMemoryUsage();
        return null;
    }
}
