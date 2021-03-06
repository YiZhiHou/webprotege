package edu.stanford.bmir.protege.web.server.change.description;

import com.google.auto.value.AutoValue;
import edu.stanford.bmir.protege.web.server.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
@AutoValue
public abstract class RemovedLanguageTag implements StructuredChangeDescription {

    @Nonnull
    public static RemovedLanguageTag get(@Nonnull IRI subject,
                                       @Nonnull OWLAnnotationProperty property,
                                       @Nonnull OWLAnnotationValue value,
                                       @Nonnull String languageTag) {
        return new AutoValue_RemovedLanguageTag(subject,
                                              property,
                                              value,
                                              languageTag);
    }

    public abstract IRI getSubject();

    public abstract OWLAnnotationProperty getProperty();

    public abstract OWLAnnotationValue getValue();

    public abstract String getLanguageTag();

    @Nonnull
    @Override
    public String getTypeName() {
        return "RemovedLanguageTag";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("从 %s 中移除了注解 %s 的语言标签 %s ",
                getSubject(),
                getProperty(),
                getLanguageTag());
    }
}
