package edu.stanford.bmir.protege.web.server.frame;

import com.google.common.collect.ImmutableSet;
import edu.stanford.bmir.protege.web.server.renderer.ContextRenderer;
import edu.stanford.bmir.protege.web.shared.entity.*;
import edu.stanford.bmir.protege.web.shared.frame.*;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-13
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassExpressionTranslator_TestCase {

    @Mock
    private ContextRenderer renderer;

    @Mock
    private OWLObjectPropertyData renderedObjectProperty;

    @Mock
    private OWLClassData renderedClass, renderedOtherClass;

    @Mock
    private OWLNamedIndividualData renderedIndividual;

    @Mock
    private OWLDatatypeData renderedDatatype;

    @Mock
    private OWLDataPropertyData renderedDataProperty;

    private final OWLObjectPropertyImpl objectProperty = new OWLObjectPropertyImpl(mock(IRI.class));

    private final OWLClassImpl cls = new OWLClassImpl(mock(IRI.class)), otherCls = new OWLClassImpl(mock(IRI.class));

    private final State initialStateAsserted = State.ASSERTED;

    private OWLNamedIndividual ind = new OWLNamedIndividualImpl(mock(IRI.class));

    private OWLDataProperty dataProperty = new OWLDataPropertyImpl(mock(IRI.class));

    private OWLDatatype datatype = new OWLDatatypeImpl(mock(IRI.class));

    private OWLLiteral literal = new OWLLiteralImplString("Hello");


    @Before
    public void setUp() {
        when(renderer.getObjectPropertyData(any())).thenReturn(renderedObjectProperty);
        when(renderer.getClassData(cls)).thenReturn(renderedClass);
        when(renderer.getClassData(otherCls)).thenReturn(renderedOtherClass);
        when(renderer.getIndividualData(any())).thenReturn(renderedIndividual);
        when(renderer.getDataPropertyData(any())).thenReturn(renderedDataProperty);
        when(renderer.getDatatypeData(any())).thenReturn(renderedDatatype);
    }

    @Test
    public void shouldTranslateObjectSomeValuesFrom() {
        var classExpression = new OWLObjectSomeValuesFromImpl(objectProperty, cls);
        var expectedPropertyValue = PropertyClassValue.get(renderedObjectProperty, renderedClass, initialStateAsserted);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateDataSomeValuesFrom() {
        var classExpression = new OWLDataSomeValuesFromImpl(dataProperty, datatype);
        var expectedPropertyValue = PropertyDatatypeValue.get(renderedDataProperty, renderedDatatype, initialStateAsserted);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateObjectHasValue() {
        var classExpression = new OWLObjectHasValueImpl(objectProperty, ind);
        var expectedPropertyValue = PropertyIndividualValue.get(renderedObjectProperty, renderedIndividual, initialStateAsserted);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateDataHasValue() {
        var classExpression = new OWLDataHasValueImpl(dataProperty, literal);
        var expectedPropertyValue = PropertyLiteralValue.get(renderedDataProperty, OWLLiteralData.get(literal), initialStateAsserted);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateObjectMinCardinality1() {
        var classExpression = new OWLObjectMinCardinalityImpl(objectProperty, 1, cls);
        var expectedPropertyValue = PropertyClassValue.get(renderedObjectProperty, renderedClass, initialStateAsserted);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateObjectMinCardinalityN() {
        var classExpression = new OWLObjectMinCardinalityImpl(objectProperty, 2, cls);
        var expectedPropertyValue = PropertyClassValue.get(renderedObjectProperty, renderedClass, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateObjectExactCardinality1() {
        var classExpression = new OWLObjectExactCardinalityImpl(objectProperty, 1, cls);
        var expectedPropertyValue = PropertyClassValue.get(renderedObjectProperty, renderedClass, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateObjectExactCardinalityN() {
        var classExpression = new OWLObjectExactCardinalityImpl(objectProperty, 2, cls);
        var expectedPropertyValue = PropertyClassValue.get(renderedObjectProperty, renderedClass, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateDataMinCardinality1() {
        var classExpression = new OWLDataMinCardinalityImpl(dataProperty, 1, datatype);
        var expectedPropertyValue = PropertyDatatypeValue.get(renderedDataProperty, renderedDatatype, initialStateAsserted);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateDataMinCardinalityN() {
        var classExpression = new OWLDataMinCardinalityImpl(dataProperty, 2, datatype);
        var expectedPropertyValue = PropertyDatatypeValue.get(renderedDataProperty, renderedDatatype, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateDataExactCardinality1() {
        var classExpression = new OWLDataExactCardinalityImpl(dataProperty, 1, datatype);
        var expectedPropertyValue = PropertyDatatypeValue.get(renderedDataProperty, renderedDatatype, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldTranslateDataExactCardinalityN() {
        var classExpression = new OWLDataExactCardinalityImpl(dataProperty, 2, datatype);
        var expectedPropertyValue = PropertyDatatypeValue.get(renderedDataProperty, renderedDatatype, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpression, initialStateAsserted, expectedPropertyValue);
    }

    @Test
    public void shouldNotTranslateObjectAllValuesFrom() {
        var property = new OWLObjectPropertyImpl(mock(IRI.class));
        var filler = new OWLClassImpl(mock(IRI.class));
        var classExpression = new OWLObjectAllValuesFromImpl(property, filler);
        assertThatExpressionIsTranslatedAsTheEmptySet(classExpression);
    }

    @Test
    public void shouldDecomposeObjectIntersectionOfAtTopLevel() {
        var subExpressionA = new OWLObjectSomeValuesFromImpl(objectProperty, cls);
        var subExpressionB = new OWLDataSomeValuesFromImpl(dataProperty, datatype);
        var classExpresion = new OWLObjectIntersectionOfImpl(ImmutableSet.of(subExpressionA, subExpressionB));
        var expectedA = PropertyClassValue.get(renderedObjectProperty, renderedClass, State.DERIVED);
        var expectedB = PropertyDatatypeValue.get(renderedDataProperty, renderedDatatype, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpresion, State.ASSERTED, ImmutableSet.of(expectedA, expectedB));
    }


    @Test
    public void shouldDecomposeObjectIntersectionOfAtFiller() {
        var filler = new OWLObjectIntersectionOfImpl(ImmutableSet.of(cls, otherCls));
        var classExpresion = new OWLObjectSomeValuesFromImpl(objectProperty, filler);
        var expectedA = PropertyClassValue.get(renderedObjectProperty, renderedClass, State.DERIVED);
        var expectedB = PropertyClassValue.get(renderedObjectProperty, renderedOtherClass, State.DERIVED);
        assertThatClassExpressionIsTranslatedAs(classExpresion, State.ASSERTED, ImmutableSet.of(expectedA, expectedB));
    }

    private void assertThatClassExpressionIsTranslatedAs(OWLClassExpression classExpression,
                                                         State expectedState,
                                                         PropertyValue expectedPropertyValues) {
        assertThatClassExpressionIsTranslatedAs(classExpression, expectedState, Collections.singleton(expectedPropertyValues));
    }

    private void assertThatExpressionIsTranslatedAsTheEmptySet(OWLClassExpression classExpression) {
        var translated = new ClassExpressionTranslator(renderer).translate(State.ASSERTED, classExpression);
        assertThat(translated.isEmpty(), Matchers.is(true));
    }

    private void assertThatClassExpressionIsTranslatedAs(OWLClassExpression classExpression,
                                                         State expectedState,
                                                         Collection<? extends PropertyValue> expectedPropertyValues) {
        var translator = new ClassExpressionTranslator(renderer);
        var translated = translator.translate(expectedState, classExpression);
        assertThat(translated, containsInAnyOrder(expectedPropertyValues.toArray()));
    }


}
