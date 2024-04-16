package io.nosqlbench.adapter.xmlgen;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.push.Document;
import net.sf.saxon.s9api.push.Element;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.A;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLGenDocBuilder implements AutoCloseable {

    private final Document document;
    private final OpenElementTree openElementTree;

    public XMLGenDocBuilder(final Document document, final Element rootElement) {
        this.document = document;
        this.openElementTree = new OpenElementTree(rootElement);
    }

    /**
     * Create an element on the supplied createPath,
     * use current open elements as parents as far down as we can go,
     * and create empty elements below that point.
     * If the open leaf element is the same element, close it.
     * Leave the element we create open.
     *
     * @param createPath at which a new element should exist
     * @return the newly created element
     */
    public Element element(final List<QName> createPath, final XMLGenElement elementContents) {

        final var element = openElementTree.replace(createPath);
        setContentsOfElement(element, elementContents);
        return element;
    }

    /**
     * Close all the elements currently open
     */
    public void close() {

        openElementTree.close();
        try {
            document.close();
        } catch (SaxonApiException e) {
            throw new RuntimeException("Error closing document " + document, e);
        }
    }

    private void setContentsOfElement(final Element element, final XMLGenElement elementContents) {

        for (var attr : elementContents.attrs().entrySet()) {
            try {
                element.attribute(attr.getKey(), attr.getValue().toString());
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file attribute " + attr.getKey(), e);
            }
        }

        for (var el : elementContents.children().entrySet()) {
            // add a recursive element and close it
            // if it is a list of values, turn it into a sequence of elements, one per list item
            var value = el.getValue();
            if (value instanceof List<?> values) {
                for (var instance : values) {
                    createChildOfElement(element, el.getKey(), instance);
                }
            } else {
                createChildOfElement(element, el.getKey(), value);
            }
        }

        try {
            element.text(elementContents.body());
        } catch (SaxonApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fluent method to set contents of an element recursively
     * @param element the contents of which to set
     * @param contents the map, or simple value, to set the contents to
     * @return the input element, after contents have been set
     */
    private List<Element> createChildOfElement(final Element element, final String label, final Object contents) {
        var result = new ArrayList<Element>(1);
        if (contents instanceof Map<?,?> contentMap) {
            Map<String, Object> children = HashMap.newHashMap(4);
            Map<String, Object> attrs = HashMap.newHashMap(4);
            String body = "";
            if (contentMap.containsKey(Keyword.BODY.label)) {
                body = (String) contentMap.remove(Keyword.BODY.label);
            }
            if (contentMap.containsKey(Keyword.CHILDREN.label)) {
                var explicitChildren = contentMap.remove(Keyword.CHILDREN.label);
                if (explicitChildren instanceof Map<?, ?> childrenMap) {
                    for (var child : childrenMap.entrySet()) {
                        children.put((String) child.getKey(), child.getValue());
                    }
                }
            }
            if (contentMap.containsKey(Keyword.ATTRS.label)) {
                var explicitAttrs = contentMap.remove(Keyword.ATTRS.label);
                if (explicitAttrs instanceof Map<?, ?> attrsMap) {
                    for (var attr : attrsMap.entrySet()) {
                        attrs.put((String) attr.getKey(), attr.getValue());
                    }
                }
            }

            // Create as many siblings as there are entries in this list
            // Usually 1, special case is __foreach
            List<XMLGenElement> createSiblings = new ArrayList<>(1);
            var explicitForeach = contentMap.remove(Keyword.FOREACH.label);

            // Entries in the map with all other keys are considered children
            children.putAll((Map<? extends String, ?>) contentMap);

            var xmlgenElement = new XMLGenElement(children, attrs, body);
            if (explicitForeach == null) {
                createSiblings.add(xmlgenElement);
            } else {
                if (explicitForeach instanceof  List<?> foreachSpecList && foreachSpecList.size() == 2) {
                    var key = foreachSpecList.get(0);
                    var foreachItemList = foreachSpecList.get(1);
                    if (foreachItemList instanceof List<?> foreachItems) {
                        for (var foreachItem : foreachItems) {
                            createSiblings.add(xmlgenElement.substitute(List.of(Pair.of("[" + key + "]", foreachItem))));
                        }
                    } else {
                        throw new RuntimeException("XML gen file element child of " + element + " as: " + contents +
                            " " + Keyword.FOREACH.label + " __foreach values is not a list: " + foreachItemList);
                    }
                } else {
                    throw new RuntimeException("XML gen file element child of " + element + " as: " + contents +
                        " " + Keyword.FOREACH.label + " must be a 2-element list key values");
                }
            }

            try {
                for (var create : createSiblings) {
                    var child = element.element(label);
                    setContentsOfElement(child, create);
                    result.add(child);
                }
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file element child of " + element + " as: " + contents, e);
            }
        } else {
            // non-map case, just terminate here with a simple element
            try {
                var child = element.element(label);
                child.text(contents.toString());
                result.add(child);
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file element contents of " + element + " as: " + contents, e);
            }
        }
        return result;
    }

}
