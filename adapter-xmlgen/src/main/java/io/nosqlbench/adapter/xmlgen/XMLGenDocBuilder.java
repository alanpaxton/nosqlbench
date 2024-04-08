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

import java.util.ArrayList;
import java.util.List;

public class XMLGenDocBuilder implements AutoCloseable {

    private final Document document;
    private final Element rootElement;
    private List<PathEntry> elementPath = new ArrayList<>();

    public XMLGenDocBuilder(final Document document, final Element rootElement) {
        this.document = document;
        this.rootElement = rootElement;
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

        int pos = 0;
        while(pos < elementPath.size() && pos < createPath.size() - 1) {
            if (!elementPath.get(pos).element.equals(createPath.get(pos))) break;
            pos++;
        }
        final int elementsDiverge = pos;
        for (int i = elementPath.size()-1; i >= elementsDiverge; i--) {
            closeElement(i);
        }
        elementPath = new ArrayList<>(elementPath.subList(0, elementsDiverge));
        for (int i = elementsDiverge; i < createPath.size(); i++) {
            addElement(createPath.get(i));
        }
        final var element = elementPath.getLast().element();
        setContentsOfElement(element, elementContents);
        return element;
    }

    /**
     * Close all the elements currently open
     */
    public void close() {
        for (int i = elementPath.size() - 1; i >= 0; i--) {
            closeElement(i);
        }
        elementPath.clear();
    }

    private void closeElement(final int elementIndex) {
        try {
            elementPath.get(elementIndex).element.close();
        } catch (SaxonApiException e) {
            throw new RuntimeException("Error closing element " + elementPath.get(elementIndex), e);
        }
    }

    private void addElement(QName qName) {

        try {
            final Element parent = elementPath.isEmpty() ? rootElement : elementPath.getLast().element();
            elementPath.add(new PathEntry(parent.element(qName), qName));
        } catch (SaxonApiException e) {
            throw new RuntimeException("Error creating element " + qName, e);
        }
    }

    private void setContentsOfElement(final Element element, final XMLGenElement elementContents) {

        for (var attr : elementContents.attrs().entrySet()) {
            try {
                element.attribute(attr.getKey(), attr.getValue().toString());
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file attribute " + attr.getKey() + " on element " + elementPath, e);
            }
        }

        for (var el : elementContents.children().entrySet()) {
            try {
                // add a simple element and close it
                // TODO (AP) recursively define child elements with List<Map<String, Object>>
                element.element(el.getKey()).text(el.getValue().toString()).close();
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file element " + el.getKey() + " on element " + elementPath, e);
            }
        }

        try {
            element.text(elementContents.body());
        } catch (SaxonApiException e) {
            throw new RuntimeException(e);
        }
    }

    record PathEntry (Element element, QName qName) {}
}
