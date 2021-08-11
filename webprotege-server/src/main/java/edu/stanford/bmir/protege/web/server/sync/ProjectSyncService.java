package edu.stanford.bmir.protege.web.server.sync;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Striped;
import edu.stanford.bmir.protege.web.server.download.*;
import edu.stanford.bmir.protege.web.server.project.ProjectDetailsManager;
import edu.stanford.bmir.protege.web.server.revision.HeadRevisionNumberFinder;
import edu.stanford.bmir.protege.web.shared.inject.ApplicationSingleton;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Sync Service
 */
@ApplicationSingleton
public class ProjectSyncService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSyncService.class);

    @Nonnull
    private final ExecutorService syncGeneratorExecutor;

    @Nonnull
    private final ExecutorService fileTransferExecutor;

    @Nonnull
    private final ProjectDetailsManager projectDetailsManager;

    @Nonnull
    private final HeadRevisionNumberFinder headRevisionNumberFinder;

    private final Striped<Lock> lockStripes = Striped.lazyWeakLock(10);

    @Nonnull
    private final CreateSyncTaskFactory createSyncTaskFactory;

    @Inject
    public ProjectSyncService(
            @Nonnull @SyncGeneratorExecutor ExecutorService syncGeneratorExecutor,
            @Nonnull @FileTransferExecutor ExecutorService fileTransferExecutor,
            @Nonnull ProjectDetailsManager projectDetailsManager,
            @Nonnull HeadRevisionNumberFinder headRevisionNumberFinder,
            @Nonnull CreateSyncTaskFactory createSyncTaskFactory
    ) {
        this.syncGeneratorExecutor = checkNotNull(syncGeneratorExecutor);
        this.fileTransferExecutor = checkNotNull(fileTransferExecutor);
        this.projectDetailsManager = checkNotNull(projectDetailsManager);
        this.headRevisionNumberFinder = checkNotNull(headRevisionNumberFinder);
        this.createSyncTaskFactory = checkNotNull(createSyncTaskFactory);
    }

    public void syncProject(@Nonnull UserId requester,
                            @Nonnull ProjectId projectId,
                            @Nonnull RevisionNumber revisionNumber,
                            @Nonnull DownloadFormat downloadFormat,
                            @Nonnull HttpServletResponse response) throws IOException {

        RevisionNumber realRevisionNumber;

        createSync(requester,
                projectId,
                revisionNumber,
                downloadFormat);
    }

    private void createSync(@Nonnull UserId requester,
                                           @Nonnull ProjectId projectId,
                                           @Nonnull RevisionNumber revisionNumber,
                                           @Nonnull DownloadFormat downloadFormat) {
        // This thing always returns the same lock for the same project.
        // This means that we won't create the *same* download more than once.  It
        // does mean that multiple *different* downloads could possibly be created at the same time
        Lock lock = lockStripes.get(projectId);
        try {
            lock.lock();

            CreateSyncTask task = createSyncTaskFactory.create(projectId,
                    requester,
                    getProjectDisplayName(projectId),
                    revisionNumber,
                    downloadFormat);
            try {
                var futureOfCreateSync = syncGeneratorExecutor.submit(task);
                logger.info("{} {} Submitted request to create download to queue", projectId, requester);
                var stopwatch = Stopwatch.createStarted();
                logger.info("{} {} Waiting for download to be created", projectId, requester);
                futureOfCreateSync.get();
                logger.info("{} {} Created download after {} ms", projectId, requester, stopwatch.elapsed(MILLISECONDS));
            } catch (RejectedExecutionException e) {
                logger.info("{} {} Generate download request rejected", projectId, requester);
            } catch (InterruptedException e) {
                logger.info("{} {} The download of this project was interrupted.", projectId, requester);
            } catch (ExecutionException e) {
                logger.info("{} {} An execution exception occurred whilst creating the download.  Cause: {}",
                        projectId,
                        requester,
                        Optional.ofNullable(e.getCause()).map(Throwable::getMessage).orElse(""),
                        e.getCause());
            }
        } finally {
            lock.unlock();
        }
    }

    private String getProjectDisplayName(@Nonnull ProjectId projectId) {
        return projectDetailsManager.getProjectDetails(projectId)
                .getDisplayName();
    }

    /**
     * Shuts down this {@link ProjectSyncService}.
     */
    public void shutDown() {
        logger.info("Shutting down Project Sync Service");
        syncGeneratorExecutor.shutdown();
        fileTransferExecutor.shutdown();
        logger.info("Project Sync Service has been shut down");
    }

    private RevisionNumber getHeadRevisionNumber(@Nonnull ProjectId projectId, @Nonnull UserId userId) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        RevisionNumber headRevisionNumber = headRevisionNumberFinder.getHeadRevisionNumber(projectId);
        logger.info("{} {} Computed head revision number ({}) in {} ms",
                projectId,
                userId,
                headRevisionNumber,
                stopwatch.elapsed(MILLISECONDS));
        return headRevisionNumber;

    }

}
