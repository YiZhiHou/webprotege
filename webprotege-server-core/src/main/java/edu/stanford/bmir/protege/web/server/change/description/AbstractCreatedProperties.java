package edu.stanford.bmir.protege.web.server.change.description;

import com.google.common.collect.ImmutableSet;
import edu.stanford.bmir.protege.web.server.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public abstract class AbstractCreatedProperties implements StructuredChangeDescription {


    public abstract ImmutableSet<? extends OWLProperty> getProperties();

    public abstract ImmutableSet<? extends OWLProperty> getParentProperties();

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        if(getParentProperties().isEmpty()) {
            if(getProperties().size() == 1) {
                return formatter.formatString("创建了属性 %s", getProperties());
            }
            else {
                return formatter.formatString("创建了属性", getProperties());
            }
        }
        else {
            if(getProperties().size() == 1) {
                return formatter.formatString("创建了 %s 的子属性 %s", getParentProperties(), getProperties());
            }
            else {
                return formatter.formatString("创建了 %s 的子属性 %s", getParentProperties(), getProperties());
            }
        }
    }
}
