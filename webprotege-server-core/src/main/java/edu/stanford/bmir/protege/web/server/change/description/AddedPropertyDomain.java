package edu.stanford.bmir.protege.web.server.change.description;

import com.google.auto.value.AutoValue;
import edu.stanford.bmir.protege.web.server.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
@AutoValue
public abstract class AddedPropertyDomain implements StructuredChangeDescription {

    public static AddedPropertyDomain get(@Nonnull OWLProperty property,
                                         @Nonnull OWLObject range) {
        return new AutoValue_AddedPropertyDomain(property, range);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedPropertyDomain";
    }

    @Nonnull
    public abstract OWLProperty getProperty();

    @Nonnull
    public abstract OWLObject getDomain();

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("为 %s 添加了定义域 %s ", getProperty(), getDomain());
    }
}
