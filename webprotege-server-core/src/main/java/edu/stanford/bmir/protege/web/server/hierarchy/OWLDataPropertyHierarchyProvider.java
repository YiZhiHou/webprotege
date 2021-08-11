package edu.stanford.bmir.protege.web.server.hierarchy;


import edu.stanford.bmir.protege.web.server.index.*;
import edu.stanford.bmir.protege.web.shared.inject.ProjectSingleton;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.SUB_DATA_PROPERTY;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Jan-2007<br><br>
 */
@ProjectSingleton
public class OWLDataPropertyHierarchyProvider extends AbstractOWLPropertyHierarchyProvider<OWLDataProperty> {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    @Nonnull
    private SubDataPropertyAxiomsBySubPropertyIndex subDataPropertyAxiomsBySubPropertyIndex;

    @Inject
    public OWLDataPropertyHierarchyProvider(ProjectId projectId,
                                            @DataPropertyHierarchyRoot OWLDataProperty root,
                                            @Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                            @Nonnull AxiomsByTypeIndex axiomsByTypeIndex,
                                            @Nonnull OntologySignatureByTypeIndex ontologySignatureByTypeIndex,
                                            @Nonnull SubDataPropertyAxiomsBySubPropertyIndex subDataPropertyAxiomsBySubPropertyIndex,
                                            @Nonnull EntitiesInProjectSignatureIndex entitiesInProjectSignatureIndex) {
        super(projectId, root, entitiesInProjectSignatureIndex, EntityType.DATA_PROPERTY, projectOntologiesIndex,
              ontologySignatureByTypeIndex);
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
        this.subDataPropertyAxiomsBySubPropertyIndex = checkNotNull(subDataPropertyAxiomsBySubPropertyIndex);
    }

    @Override
    protected String getHierarchyName() {
        return "data property";
    }



    @Override
    public Set<OWLDataProperty> getChildren(OWLDataProperty property) {
        rebuildIfNecessary();
        if(getRoot().equals(property)) {
            return getChildrenOfRoot();
        }
        return projectOntologiesIndex.getOntologyIds()
                                     .flatMap(ontId -> axiomsByTypeIndex.getAxiomsByType(SUB_DATA_PROPERTY, ontId))
                                     .filter(ax -> ax.getSuperProperty().equals(property))
                                     .map(OWLSubDataPropertyOfAxiom::getSubProperty)
                                     .filter(OWLDataPropertyExpression::isNamed)
                                     .map(OWLDataPropertyExpression::asOWLDataProperty)
                                     .collect(toSet());
    }

    @Override
    public Set<OWLDataProperty> getParents(OWLDataProperty property) {
        rebuildIfNecessary();
        return projectOntologiesIndex.getOntologyIds()
                                     .flatMap(ontId -> subDataPropertyAxiomsBySubPropertyIndex.getSubPropertyOfAxioms(
                                             property,
                                             ontId))
                                     .map(OWLSubPropertyAxiom::getSuperProperty)
                                     .filter(OWLDataPropertyExpression::isNamed)
                                     .map(OWLDataPropertyExpression::asOWLDataProperty)
                                     .collect(toSet());
    }

    @Override
    public Set<OWLDataProperty> getEquivalents(OWLDataProperty property) {
        rebuildIfNecessary();
        return projectOntologiesIndex.getOntologyIds()
                                     .flatMap(ontId -> axiomsByTypeIndex.getAxiomsByType(EQUIVALENT_DATA_PROPERTIES,
                                                                                         ontId))
                                     .map(OWLEquivalentDataPropertiesAxiom::getProperties)
                                     .flatMap(Collection::stream)
                                     .filter(OWLDataPropertyExpression::isNamed)
                                     .filter(propertyExpression -> !propertyExpression.equals(property))
                                     .map(OWLDataPropertyExpression::asOWLDataProperty)
                                     .collect(toSet());
    }
}
