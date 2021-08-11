package edu.stanford.bmir.protege.web.server.search;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.shared.pagination.Page;
import edu.stanford.bmir.protege.web.shared.pagination.PageRequest;
import edu.stanford.bmir.protege.web.shared.search.EntitySearchResult;
import edu.stanford.bmir.protege.web.shared.search.PerformEntitySearchAction;
import edu.stanford.bmir.protege.web.shared.search.PerformEntitySearchResult;
import org.semanticweb.owlapi.model.EntityType;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 21 Apr 2017
 */
public class PerformEntitySearchActionHandler extends AbstractProjectActionHandler<PerformEntitySearchAction, PerformEntitySearchResult> {

    @Nonnull
    private final EntitySearcherFactory entitySearcherFactory;

    @Inject
    public PerformEntitySearchActionHandler(@Nonnull AccessManager accessManager,
                                            @Nonnull EntitySearcherFactory entitySearcherFactory) {
        super(accessManager);
        this.entitySearcherFactory = entitySearcherFactory;
    }

    @Nonnull
    @Override
    public Class<PerformEntitySearchAction> getActionClass() {
        return PerformEntitySearchAction.class;
    }

    @Nonnull
    @Override
    public PerformEntitySearchResult execute(@Nonnull PerformEntitySearchAction action,
                                             @Nonnull ExecutionContext executionContext) {
        Set<EntityType<?>> entityTypes = action.getEntityTypes();
        String searchString = action.getSearchString();
        EntitySearcher entitySearcher = entitySearcherFactory.create(entityTypes,
                                                                     searchString,
                                                                     executionContext.getUserId());
        PageRequest pageRequest = action.getPageRequest();
        int pageSize = pageRequest.getPageSize();
        entitySearcher.setLimit(pageSize);

        int pageNumber = pageRequest.getPageNumber();
        entitySearcher.setSkip((pageNumber - 1) * pageSize);

        entitySearcher.invoke();

        int totalSearchResults = entitySearcher.getSearchResultsCount();
        List<EntitySearchResult> results = entitySearcher.getResults();
        int pageCount = (totalSearchResults / pageSize) + 1;
        Page<EntitySearchResult> page = new Page<>(pageNumber > pageCount ? 1 : pageNumber,
                                                   pageCount, results, totalSearchResults);
        return new PerformEntitySearchResult(searchString, totalSearchResults, page);
    }
}

