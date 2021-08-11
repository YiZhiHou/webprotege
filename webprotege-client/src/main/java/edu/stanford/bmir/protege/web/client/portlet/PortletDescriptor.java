package edu.stanford.bmir.protege.web.client.portlet;

import edu.stanford.bmir.protege.web.shared.PortletId;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/16
 */
public class PortletDescriptor {

    private final PortletId portletId;

    private final String title;

    private final String tooltip;


    public PortletDescriptor(PortletId portletId, String title, String tooltip) {
        this.portletId = checkNotNull(portletId);
        this.title = checkNotNull(Translate(title));
        this.tooltip = checkNotNull(Translate(tooltip));
    }

    public PortletId getPortletId() {
        return portletId;
    }

    public String getTitle() {
        return title;
    }

    public String getTooltip() {
        return tooltip;
    }

    private String Translate(String originalText){
        switch(originalText){
            case "Changes By Entity":
                return "实体变更";
            case "Displays a list of project changes for the selected entity.":
                return "显示选定实体的项目变更列表";
            case "Project History":
                return "项目历史";
            case "Displays a list of all changes that have been made to the project":
                return "显示对项目所做的所有变更的列表";
            case "Class":
                return "类";
            case "Provides an editor that allows class descriptions to be edited":
                return "提供一个可以编辑类描述的编辑器";
            case "Entity Editor":
                return "实体编辑器";
            case "Displays a simple property-value oriented description of the selected class, property or individual for viewing and editing.":
                return "对所选的类、属性或个体显示一个面向属性-值的简单描述，以供查看和编辑。";
            case "Individual":
                return "个体";
            case "Provides an editor that allows individual descriptions to be edited":
                return "提供一个可以编辑个体描述的编辑器";
            case "Property":
                return "属性";
            case "Provides an editor that allows property descriptions to be edited":
                return "提供一个可以编辑属性描述的编辑器";
            case "Deprecated Entities":
                return "已弃用的实体";
            case "Displays a list of entities that are marked as deprecated":
                return "显示标记为已弃用的实体列表";
            case "OWL Entity Description Editor":
                return "OWL实体描述编辑器";
            case "Allows the description of the selected entity to be edited in Manchester Syntax.  The complete OWL 2 syntax is supported.":
                return "允许使用Manchester Syntax语法编辑所选实体的描述，同时支持完整的OWL 2语法。";
            case "Class Hierarchy":
                return "类层次结构";
            case "Displays the class hierarchy as a tree.":
                return "将类层次结构显示为树状结构";
            case "Property Hierarchy":
                return "属性层次结构";
            case "Displays the object, data and annotation property hierarchies as a tree.":
                return "将对象、数据和注解属性的层次结构显示为树状结构";
            case "Individuals by Class":
                return "类个体";
            case "Display Individuals by Class":
                return "显示类的个体";
            case "Commented Entities":
                return "已注释实体";
            case "Displays a list of commented entities":
                return "显示已注释实体的列表";
            case "Comments":
                return "评论";
            case "Displays comments for the selected entity":
                return "显示选定实体的评论";
            case "Query":
                return "查询";
            case "Allows asserted information to be queried":
                return "可以查询断言信息";
            case "OBO Term Cross Product":
                return "OBO术语叉积";
            case "OBO Term Definition":
                return "OBO术语定义";
            case "OBO Term Id":
                return "OBO术语Id";
            case "OBO Term Relationships":
                return "OBO术语关系";
            case "OBO Term Synonyms":
                return "OBO术语同义词";
            case "OBO Term XRefs":
                return "OBO术语XRefs";
            case "Ontology Annotations":
                return "本体注解";
            case "Ontology Id":
                return "本体Id";
            case "Project Feed":
                return "项目提要";
            case "OWL Entity Description Browser":
                return "OWL实体描述浏览器";
            case "Entity Usage":
                return "实体用法";
            case "Displays the usage for the selected class, property or individual.":
                return "显示所选类、属性或个体的用法。";
            case "Entity Graph":
                return "实体图";
            case "Provides a visualisation":
                return "提供可视化展示";
            case "Watched Entities":
                return "已监视的实体";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
//            case "":
//                return "";
            default:
                return originalText;
        }
    }
}
