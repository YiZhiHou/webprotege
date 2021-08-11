package edu.stanford.bmir.protege.web.server.crud.uuid;

import edu.stanford.bmir.protege.web.server.change.OntologyChange;
import edu.stanford.bmir.protege.web.server.change.OntologyChangeList;
import edu.stanford.bmir.protege.web.server.crud.ChangeSetEntityCrudSession;
import edu.stanford.bmir.protege.web.server.crud.EntityCrudContext;
import edu.stanford.bmir.protege.web.server.crud.PrefixedNameExpander;
import edu.stanford.bmir.protege.web.server.index.EntitiesInProjectSignatureByIriIndex;
import edu.stanford.bmir.protege.web.shared.crud.EntityCrudKitPrefixSettings;
import edu.stanford.bmir.protege.web.shared.crud.EntityShortForm;
import edu.stanford.bmir.protege.web.shared.crud.uuid.UUIDSuffixSettings;
import edu.stanford.bmir.protege.web.shared.shortform.DictionaryLanguage;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.bmir.protege.web.server.OWLDeclarationAxiomMatcher.declarationFor;
import static edu.stanford.bmir.protege.web.server.OWLEntityMatcher.hasIRI;
import static edu.stanford.bmir.protege.web.server.OWLEntityMatcher.owlThing;
import static edu.stanford.bmir.protege.web.server.RdfsLabelWithLexicalValueAndLang.rdfsLabelWithLexicalValueAndLang;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 16/04/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class UUIDEntityCrudKitHandlerTestCase {

    public static final String PREFIX = "http://stuff/";
    @Mock
    protected EntityCrudKitPrefixSettings prefixSettings;

    @Mock
    protected UUIDSuffixSettings suffixSettings;

    @Mock
    protected EntityCrudContext crudContext;

    @Mock
    protected EntityShortForm entityShortForm;

    @Mock
    protected OWLOntology ontology;

    @Mock
    protected OntologyChangeList.Builder<OWLClass> builder;

    @Mock
    protected ChangeSetEntityCrudSession session;

    @Mock
    protected DictionaryLanguage dictionaryLanguage;

    protected IRI annotationPropertyIri = OWLRDFVocabulary.RDFS_LABEL.getIRI();

    private UUIDEntityCrudKitHandler handler;

    private OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    @Mock
    private EntitiesInProjectSignatureByIriIndex entitiesInSignature;

    @Mock
    private OWLOntologyID ontologyId;

    @Before
    public void setUp() throws Exception {
        when(prefixSettings.getIRIPrefix()).thenReturn(PREFIX);
        when(crudContext.getTargetOntologyId()).thenReturn(ontologyId);
        when(crudContext.getDictionaryLanguage()).thenReturn(dictionaryLanguage);
        when(dictionaryLanguage.getLang()).thenReturn("en");
        when(dictionaryLanguage.getAnnotationPropertyIri()).thenReturn(annotationPropertyIri);
        when(crudContext.getPrefixedNameExpander()).thenReturn(PrefixedNameExpander.builder().withNamespaces(Namespaces.values()).build());
        handler = new UUIDEntityCrudKitHandler(prefixSettings, suffixSettings, dataFactory, entitiesInSignature);
        when(entitiesInSignature.getEntitiesInSignature(any()))
                .thenAnswer(invocation -> Stream.empty());
    }

    @Test
    public void shouldGetSuppliedPrefixSettings() {
        assertThat(handler.getPrefixSettings(), is(prefixSettings));
    }

    @Test
    public void shouldGetSuppliedSuffixSettings() {
        assertThat(handler.getSuffixSettings(), is(suffixSettings));
    }

    @Test
    public void shouldGetSettings() {
        var settings = handler.getSettings();
        assertThat(settings.getPrefixSettings(), is(prefixSettings));
        assertThat(settings.getSuffixSettings(), is(suffixSettings));
    }

    @Test
    public void shouldGetKitId() {
        var kitId = handler.getKitId();
        assertThat(kitId, is(suffixSettings.getKitId()));
    }

    @Test
    public void shouldCreateSession() {
        var session = handler.createChangeSetSession();
        assertThat(session, is(not(nullValue())));
    }

    @Test
    public void shouldAddDeclaration() {
        when(entityShortForm.getShortForm()).thenReturn("A");
        OWLClass cls = handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), crudContext, builder);
        var ontologyChangeCaptor = ArgumentCaptor.forClass(OntologyChange.class);
        verify(builder, atLeast(1)).add(ontologyChangeCaptor.capture());
        var addedAxioms = ontologyChangeCaptor.getAllValues()
                                              .stream()
                                              .map(OntologyChange::getAxiomOrThrow)
                                              .filter(ax -> ax instanceof OWLDeclarationAxiom)
                                              .map(ax -> (OWLDeclarationAxiom) ax)
                                              .collect(Collectors.toList());
        assertThat(addedAxioms, hasItem(Matchers.is(declarationFor(cls))));
    }

    @Test
    public void shouldAddLabelEqualToSuppliedName() {
        String suppliedName = "MyLabel";
        when(entityShortForm.getShortForm()).thenReturn(suppliedName);
        handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), crudContext, builder);
        verifyHasLabelEqualTo(suppliedName, "en");
    }

    @Test
    public void shouldCreatedExpandedPrefixName() {
        when(entityShortForm.getShortForm()).thenReturn("owl:Thing");
        OWLClass cls = handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), crudContext, builder);
        assertThat(cls, is(owlThing()));
    }

    @Test
    public void shouldAddLabelEqualToPrefixedName() {
        String suppliedName = "owl:Thing";
        when(entityShortForm.getShortForm()).thenReturn(suppliedName);
        handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), crudContext, builder);
        verifyHasLabelEqualTo(suppliedName, "en");
    }

    @Test
    public void shouldCreateEntityWithAbsoluteIriIfSpecified() {
        String expectedIRI = "http://stuff.com/A";
        String shortForm = "<" + expectedIRI + ">";
        when(entityShortForm.getShortForm()).thenReturn(shortForm);
        OWLClass cls = handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), crudContext, builder);
        assertThat(cls, hasIRI(expectedIRI));
        verifyHasLabelEqualTo(expectedIRI, "en");
    }


    private void verifyHasLabelEqualTo(String label, String langTag) {
        var addAxiomCaptor = ArgumentCaptor.forClass(OntologyChange.class);
        verify(builder, atLeast(1)).add(addAxiomCaptor.capture());
        List<OWLAxiom> addedAxioms = addAxiomCaptor.getAllValues().stream()
                                                   .map(OntologyChange::getAxiomOrThrow)
                                                   .collect(Collectors.toList());
        assertThat(addedAxioms, hasItem(rdfsLabelWithLexicalValueAndLang(label, langTag)));
    }


}
