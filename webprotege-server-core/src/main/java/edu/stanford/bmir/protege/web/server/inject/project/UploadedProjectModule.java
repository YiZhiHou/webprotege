package edu.stanford.bmir.protege.web.server.inject.project;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import dagger.Module;
import dagger.Provides;
import edu.stanford.bmir.protege.web.server.index.*;
import edu.stanford.bmir.protege.web.server.lang.LanguageManager;
import edu.stanford.bmir.protege.web.server.mansyntax.render.*;
import edu.stanford.bmir.protege.web.server.project.Ontology;
import edu.stanford.bmir.protege.web.server.renderer.LiteralLexicalFormTransformer;
import edu.stanford.bmir.protege.web.server.renderer.ShortFormAdapter;
import edu.stanford.bmir.protege.web.server.shortform.DictionaryUpdater;
import edu.stanford.bmir.protege.web.server.shortform.MultiLingualDictionary;
import edu.stanford.bmir.protege.web.server.shortform.MultiLingualDictionaryImpl;
import edu.stanford.bmir.protege.web.shared.inject.ProjectSingleton;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.shortform.DictionaryLanguage;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.ShortFormProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 */
@Module
public class UploadedProjectModule {

    private final ProjectId projectId;

    private final ImmutableSet<Ontology> ontologies;

    @Nonnull
    private final LanguageManager languagesManager;

    public UploadedProjectModule(@Nonnull ProjectId projectId,
                                 @Nonnull ImmutableSet<Ontology> ontologies,
                                 @Nonnull LanguageManager languagesManager) {
        this.projectId = projectId;
        this.ontologies = checkNotNull(ontologies);
        this.languagesManager = checkNotNull(languagesManager);
    }

    @Provides
    @ProjectSingleton
    ProjectId providesProjectId() {
        return projectId;
    }

    @Provides
    @ProjectSingleton
    ImmutableSet<Ontology> provideUploadedOntologies() {
        return ontologies;
    }

    @Provides
    @ProjectSingleton
    OWLDataFactoryImpl provideOwlDataFactoryImpl() {
        return new OWLDataFactoryImpl();
    }

    @Provides
    @ProjectSingleton
    OWLDataFactory provideDataFactory(OWLDataFactoryImpl impl) {
        return impl;
    }

    @Provides
    @ProjectSingleton
    OWLEntityProvider provideOwlEntityProvider(OWLDataFactory dataFactory) {
        return dataFactory;
    }

    @Provides
    @ProjectSingleton
    ProjectOntologiesIndex providesProjectOntologiesIndex(ImmutableSet<Ontology> uploadedOntologies) {
        return new ProjectOntologiesIndex() {
            @Nonnull
            @Override
            public Stream<OWLOntologyID> getOntologyIds() {
                return uploadedOntologies.stream()
                                         .map(Ontology::getOntologyId);
            }
        };
    }

    @Provides
    @ProjectSingleton
    AxiomsByTypeIndex providesAxiomsByTypeIndex(ImmutableSet<Ontology> uploadedOntologies) {
        return new AxiomsByTypeIndex() {
            @Override
            public <T extends OWLAxiom> Stream<T> getAxiomsByType(AxiomType<T> axiomType, OWLOntologyID ontologyId) {
                return uploadedOntologies.stream()
                                         .filter(ont -> ont.getOntologyId()
                                                           .equals(ontologyId))
                                         .flatMap(ont -> ont.getAxioms()
                                                            .stream())
                                         .filter(ax -> ax.getAxiomType()
                                                         .equals(axiomType))
                                         .map(ax -> (T) ax);
            }
        };
    }

    @Provides
    @ProjectSingleton
    Multimap<IRI, OWLEntity> providesEntitiesByIRIMultimap(ImmutableSet<Ontology> uploadedOntologies) {
        var iri2EntityMap = HashMultimap.<IRI, OWLEntity>create(1000, 1);
        uploadedOntologies.stream()
                          .flatMap(ont -> ont.getAxioms()
                                             .stream())
                          .flatMap(ax -> ax.getSignature()
                                           .stream())
                          .forEach(entity -> iri2EntityMap.put(entity.getIRI(), entity));
        return iri2EntityMap;
    }

    @Provides
    @ProjectSingleton
    EntitiesInProjectSignatureByIriIndex providesEntitiesInProjectSignatureByIriIndex(Multimap<IRI, OWLEntity> iri2EntityMap) {
        return new EntitiesInProjectSignatureByIriIndex() {
            @Nonnull
            @Override
            public Stream<OWLEntity> getEntitiesInSignature(@Nonnull IRI entityIri) {
                return iri2EntityMap.get(entityIri)
                                    .stream();
            }
        };
    }

    @Provides
    @ProjectSingleton
    AxiomsByEntityReferenceIndex provideAxiomsByEntityReferenceIndex() {
        return (entity, ontologyId) -> ontologies.stream()
                                         .filter(ontology -> ontology.getOntologyId().equals(ontologyId))
                                         .flatMap(ontology ->  ontology.getAxioms().stream())
                                         .filter(ax -> ax.containsEntityInSignature(entity));
    }

    @Provides
    @ProjectSingleton
    ProjectSignatureIndex provideProjectSignatureIndex(Multimap<IRI, OWLEntity> iri2EntityMap) {
        return new ProjectSignatureIndex() {
            @Nonnull
            @Override
            public Stream<OWLEntity> getSignature() {
                return iri2EntityMap.values()
                                    .stream();
            }
        };
    }

    @Provides
    @ProjectSingleton
    ProjectAnnotationAssertionAxiomsBySubjectIndex projectAnnotationAssertionAxiomsBySubjectIndex() {
        return new ProjectAnnotationAssertionAxiomsBySubjectIndex() {
            @Nonnull
            @Override
            public Stream<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(@Nonnull OWLAnnotationSubject subject) {
                return ontologies.stream()
                                 .flatMap(ont -> ont.getAxioms().stream())
                                 .filter(axiom -> axiom instanceof OWLAnnotationAssertionAxiom)
                                 .map(axiom -> (OWLAnnotationAssertionAxiom) axiom)
                                 .filter(axiom -> axiom.getSubject().equals(subject));
            }
        };
    }

    @Provides
    @ProjectSingleton
    DictionaryUpdater providesDictionaryUpdater(ProjectAnnotationAssertionAxiomsBySubjectIndex index) {
        return new DictionaryUpdater(index);
    }

    @Provides
    @ProjectSingleton
    MultiLingualDictionary providesMultiLingualDictionary(MultiLingualDictionaryImpl impl,
                                                          ImmutableList<DictionaryLanguage> dictionaryLanguages) {
        impl.loadLanguages(dictionaryLanguages);
        return impl;
    }

    @Provides
    ShortFormProvider provideShortFormProvider(ShortFormAdapter shortFormAdapter) {
        return shortFormAdapter;
    }

    @Provides
    OWLObjectRenderer provideOwlObjectRenderer(ManchesterSyntaxObjectRenderer manchesterSyntaxObjectRenderer) {
        return manchesterSyntaxObjectRenderer;
    }

    @Provides
    EntityIRIChecker provideEntityIRIChecker(Multimap<IRI, OWLEntity> iri2EntityMap) {
        return new EntityIRIChecker() {
            @Override
            public boolean isEntityIRI(IRI iri) {
                return iri2EntityMap.containsKey(iri);
            }

            @Override
            public Collection<OWLEntity> getEntitiesWithIRI(IRI iri) {
                return ImmutableList.copyOf(iri2EntityMap.get(iri));
            }
        };
    }

    @Provides
    HttpLinkRenderer provideLinkRenderer(NullHttpLinkRenderer impl) {
        return impl;
    }

    @Provides
    edu.stanford.bmir.protege.web.server.mansyntax.render.LiteralRenderer provideLiteralRenderer() {
        return (literal, stringBuilder) -> stringBuilder.append(literal);
    }

    @Provides
    LiteralLexicalFormTransformer provideLiteralLexicalFormTransformer() {
        return lexicalForm -> lexicalForm;
    }

    @Provides
    LanguageManager provideLanguageManager() {
        return languagesManager;
    }

    @Provides
    ImmutableList<DictionaryLanguage> providesDictionaryLanguages() {
        return ImmutableList.copyOf(languagesManager.getActiveLanguages());
    }

    @Provides
    LiteralStyle provideLiteralStyle() {
        return LiteralStyle.REGULAR;
    }
}
