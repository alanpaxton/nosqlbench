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
    private Element createChildOfElement(final Element element, final String label, final Object contents) {
        if (contents instanceof Map<?,?> contentMap) {
            Map<String, Object> children = HashMap.newHashMap(4);
            Map<String, Object> attrs = HashMap.newHashMap(4);
            String body = "";
            if (contentMap.containsKey("body")) {
                body = (String) contentMap.remove("body");
            }
            if (contentMap.containsKey("children")) {
                var explicitChildren = contentMap.remove("children");
                if (explicitChildren instanceof Map<?, ?> childrenMap) {
                    for (var child : childrenMap.entrySet()) {
                        children.put((String) child.getKey(), child.getValue());
                    }
                }
            }
            if (contentMap.containsKey("attrs")) {
                var explicitAttrs = contentMap.remove("attrs");
                if (explicitAttrs instanceof Map<?, ?> attrsMap) {
                    for (var attr : attrsMap.entrySet()) {
                        attrs.put((String) attr.getKey(), attr.getValue());
                    }
                }
            }
            // Entries in the map with all other keys are considered children
            children.putAll((Map<? extends String, ?>) contentMap);

            try {
                var child = element.element(label);
                setContentsOfElement(child, new XMLGenElement(children, attrs, body));
                return child;
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file element child of " + element + " as: " + contents, e);
            }
        } else {
            try {
                var child = element.element(label);
                child.text(contents.toString());
                return child;
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file element contents of " + element + " as: " + contents, e);
            }
        }
    }

    /**
     * Keep multiple elements open for update
     */
    static private class OpenElementTree {

        Element element;

        HashMap<QName, OpenElementTree> children;

        OpenElementTree(final Element element) {
            this.element = element;
            this.children = HashMap.newHashMap(2);
        }

        /**
         * Close the element on the path, and all its children
         * @param path
         */
        void close(List<QName> path) {
            if (path.isEmpty()) {
                for (var child : children.values()) {
                    child.close(path);
                }
                try {
                    element.close();
                } catch (SaxonApiException e) {
                    throw new RuntimeException("Error closing element: " + element, e);
                }
                children.clear();
            } else {
                var key = path.getFirst();
                var subTree = children.get(key);
                if (subTree != null) {
                    subTree.close(path.subList(1, path.size()));
                }
                if (path.size() == 1) {
                    children.remove(key);
                }
            }
        }

        /**
         * Close the entire tree
         */
        void close() {
            close(List.of());
        }

        /**
         * Open a path, creating subtree(s) as necessary
         *
         * @param path to open
         * @return the element on the path
         */
        Element open(List<QName> path) {
            if (path.isEmpty()) {
                return element;
            }
            var key = path.getFirst();
            var subTree = children.get(key);
            if (subTree == null) {
                Element keyElement = null;
                try {
                    keyElement = element.element(key);
                } catch (SaxonApiException e) {
                    throw new RuntimeException("Error creating element " + key + " " + e.getMessage(), e);
                }
                subTree = new OpenElementTree(keyElement);
                children.put(key, subTree);
            }
            return subTree.open(path.subList(1, path.size()));
        }

        Element replace(List<QName> path) {
            close(path);
            return open(path);
        }
    }
}
