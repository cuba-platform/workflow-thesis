/**
 *
 * <p>$Id: SubDesignPostProcessor.java 10340 2013-01-25 06:18:18Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
package com.haulmont.workflow.core.app.design.postprocessors;

import com.haulmont.workflow.core.app.design.BaseDesignPostProcessor;
import com.haulmont.workflow.core.app.design.modules.SubDesignModule;
import com.haulmont.workflow.core.error.DesignCompilationError;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubDesignPostProcessor extends BaseDesignPostProcessor {

    @Override
    public void processJpdl(Element rootElement, List<DesignCompilationError> compileErrors) {
        Map<String, String> subDesigns = new HashMap<String, String>();
        List<Element> subDesign = (List<Element>) rootElement.elements(SubDesignModule.SUBDESIGN_ELEMENT_NAME);
        if (!subDesign.isEmpty()) {
            for (Element element : subDesign) {
                subDesigns.put(element.attributeValue("name"), element.attributeValue("startTransitionName"));
            }
            for (Element element : (List<Element>) rootElement.elements()) {
                List<Element> transitions = element.elements("transition");
                if (!transitions.isEmpty()) {
                    for (Element transition : transitions) {
                        String to = transition.attributeValue("to");
                        if (StringUtils.isNotBlank(to)) {
                            String newDest = subDesigns.get(to);
                            if (newDest != null) {
                                transition.addAttribute("to", newDest);
                            }
                        }
                    }
                }
            }
        }
    }
}
