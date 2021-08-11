package edu.stanford.bmir.protege.web.server.entity;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.index.DeprecatedEntitiesByEntityIndex;
import edu.stanford.bmir.protege.web.server.index.ProjectSignatureByTypeIndex;
import edu.stanford.bmir.protege.web.server.renderer.RenderingManager;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.entity.GetDeprecatedEntitiesAction;
import edu.stanford.bmir.protege.web.shared.entity.GetDeprecatedEntitiesResult;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.pagination.Page;
import edu.stanford.bmir.protege.web.shared.pagination.PageRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Optional;

import static edu.stanford.bmir.protege.web.server.pagination.PageCollector.toPageNumber;
import static edu.stanford.bmir.protege.web.shared.access.BuiltInAction.VIEW_PROJECT;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16 Jun 2017
 */
public class GetDeprecatedEntitiesActionHandler extends AbstractProjectActionHandler<GetDeprecatedEntitiesAction, GetDeprecatedEntitiesResult> {

    @Nonnull
    private final DeprecatedEntitiesByEntityIndex deprecatedEntitiesByEntityIndex;

    @Nonnull
    private final RenderingManager renderingManager;

    @Nonnull
    private final ProjectSignatureByTypeIndex projectSignatureByTypeIndex;

    @Inject
    public GetDeprecatedEntitiesActionHandler(@Nonnull AccessManager accessManager,
                                              @Nonnull DeprecatedEntitiesByEntityIndex deprecatedEntitiesByEntityIndex,
                                              @Nonnull RenderingManager renderingManager,
                                              @Nonnull ProjectSignatureByTypeIndex projectSignatureByTypeIndex) {
        super(accessManager);
        this.deprecatedEntitiesByEntityIndex = deprecatedEntitiesByEntityIndex;
        this.renderingManager = renderingManager;
        this.projectSignatureByTypeIndex = projectSignatureByTypeIndex;
    }

    @Nonnull
    @Override
    public Class<GetDeprecatedEntitiesAction> getActionClass() {
        return GetDeprecatedEntitiesAction.class;
    }

    @Nullable
    @Override
    protected BuiltInAction getRequiredExecutableBuiltInAction() {
        return VIEW_PROJECT;
    }

    @Nonnull
    @Override
    public GetDeprecatedEntitiesResult execute(@Nonnull GetDeprecatedEntitiesAction action,
                                               @Nonnull ExecutionContext executionContext) {
        PageRequest pageRequest = action.getPageRequest();
        Optional<Page<OWLEntityData>> page =
                action.getEntityTypes()
                      .stream()
                      .flatMap(projectSignatureByTypeIndex::getSignature)
                      .filter(deprecatedEntitiesByEntityIndex::isDeprecated)
                      .map(renderingManager::getRendering)
                      .sorted()
                      .collect(toPageNumber(pageRequest.getPageNumber())
                                       .forPageSize(pageRequest.getPageSize()));
        return new GetDeprecatedEntitiesResult(page.orElse(Page.emptyPage()));
    }


}
