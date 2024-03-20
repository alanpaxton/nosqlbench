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

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.push.Document;
import net.sf.saxon.s9api.push.Element;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XMLGenContext {

    private static Processor singletonProcessor;

    synchronized private static Processor getProcesser() {
        if (singletonProcessor == null) {
            singletonProcessor = new Processor();
        }
        return singletonProcessor;
    }

    private final File outputDirectory;
    private final Map<Long, Pair<Document, Element>> files = new HashMap<>();

    private final String rootNode;

    /**
     *
     * @param directorypath path at which to generate files
     * @param rootNode the XML element of the tree root in each generated document
     */
    public XMLGenContext(final String directorypath, final String rootNode) {
        var file = new File(directorypath).getAbsoluteFile();
        if (file.exists() && !file.isDirectory()) {
            throw new RuntimeException("XML Generation, root directory is not a directory: " + file.getPath());
        }
        if (file.exists()) {
            try {
                FileUtils.cleanDirectory(file);
            } catch (IOException e) {
                throw new RuntimeException("XML Generation, could not clean output directory: " + file.getPath(), e);
            }
        } else {
            try {
                FileUtils.createParentDirectories(file);
            } catch (IOException e) {
                throw new RuntimeException("XML Generation, could create path to output directory: " + file.getPath(), e);
            }
            if (!file.mkdir()) {
                throw new RuntimeException("XML Generation, could create output directory: " + file.getPath());
            }
        }
        this.outputDirectory = file;
        this.rootNode = rootNode;
    }

    /**
     *
     */
    public void close() {

        for (var file : files.entrySet()) {
            try {
                file.getValue().getFirst().close();
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file close " + file.getKey(), e);
            }
        }
    }

    public Object createElement(final Long fileIndex, final String elementName, final Map<String, Object> attrs, final String body) {

        final var rootElement = getDocumentRoot(fileIndex);
        synchronized (rootElement) {
            try {
                final var element = rootElement.element(elementName);
                for (var attr : attrs.entrySet()) {
                    try {
                        element.attribute(attr.getKey(), attr.getValue().toString());
                    } catch (SaxonApiException e) {
                        throw new RuntimeException("XML gen file attribute " + attr.getKey() + " on element " + elementName, e);
                    }
                }
                element.text(body).close();
                return element;
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file element " + elementName, e);
            }
        }
    }

    /**
     * Get hold of the root element of the file at the supplied index
     * Create the file on first access, assuming it does not already exist,
     * and build the initial (root) element.
     *
     * @param fileIndex index of the XML file being created
     * @return the root element of the XML file for the supplied fileIndex
     */
    synchronized private Element getDocumentRoot(long fileIndex) {
        var pair = files.computeIfAbsent(fileIndex, newFile -> {
            final var file = new File(outputDirectory, "xml" + fileIndex + ".xml");
            try {
                if (!file.createNewFile()) {
                    throw new RuntimeException("XML gen file " + file + " already exists.");
                }
            } catch (IOException e) {
                throw new RuntimeException("XML gen file " + file + " could not be created.", e);
            }
            try {
                final var pushDocument = getProcesser().newPush(getProcesser().newSerializer(file)).document(true);
                return Pair.create(pushDocument, pushDocument.element(rootNode));
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file " + file + " could not create serialization.");
            }
        });
        return pair.getSecond();
    }
}
