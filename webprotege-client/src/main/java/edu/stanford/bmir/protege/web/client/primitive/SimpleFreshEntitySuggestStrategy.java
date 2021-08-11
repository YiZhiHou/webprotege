package edu.stanford.bmir.protege.web.client.primitive;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import edu.stanford.bmir.protege.web.client.library.suggest.EntitySuggestion;
import edu.stanford.bmir.protege.web.shared.DataFactory;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 01/03/2014
 */
public class SimpleFreshEntitySuggestStrategy implements FreshEntitySuggestStrategy {

    public SimpleFreshEntitySuggestStrategy() {
    }

    @Override
    public FreshEntitySuggestMode getMode() {
        return FreshEntitySuggestMode.SUGGEST_CREATE_FRESH_ENTITIES;
    }

    @Override
    public List<EntitySuggestion> getSuggestions(String query, List<EntityType<?>> suggestedTypes) {
        List<EntitySuggestion> suggestions = Lists.newArrayList();
        for(EntityType<?> allowedType : suggestedTypes) {
                OWLEntity entity = DataFactory.getFreshOWLEntity(allowedType, query, Optional.empty());
                OWLEntityData entityData = DataFactory.getOWLEntityData(entity, query, ImmutableMap.of());
                suggestions.add(new EntitySuggestion(entityData, formatSuggestText(query, allowedType)));
        }
        return suggestions;
    }

    private String formatSuggestText(String query, EntityType<?> allowedType) {
        return "<span class=\"new-keyword\">新的</span> <span style=\"font-weight: bold;\">" + Translate(allowedType.getPrintName()) + "</span> ，名称： " + query;
    }

    private String Translate(String originalText){
        switch (originalText){
            case "Class":
            case "Classes":
                return "类";
            case "Object property":
            case "Object properties":
                return "对象属性";
            case "Data property":
            case "Data properties":
                return "数据属性";
            case "Annotation property":
            case "Annotation properties":
                return "注解属性";
            case "Named individual":
            case "Named individuals":
                return "已命名个体";
            case "Datatype":
            case "Datatypes":
                return "数据类型";
            default:
                return "未知类型";
        }
    }
}
