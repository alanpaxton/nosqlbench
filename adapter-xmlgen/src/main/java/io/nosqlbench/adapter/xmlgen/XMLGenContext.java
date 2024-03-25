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
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLGenContext implements AutoCloseable {

    private static Processor singletonProcessor;

    /**
     * Access and possibly create the singleton Processor
     *
     * @return the shared singleton processor
     */
    synchronized private static Processor getProcesser() {
        if (singletonProcessor == null) {
            singletonProcessor = new Processor();
        }
        return singletonProcessor;
    }

    private final File outputDirectory;
    private final Map<Long, XMLGenDocBuilder> files = new HashMap<>();

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
            file.getValue().close();
        }
    }

    /**
     * <p>
     *     Synchronized to prevent separate threads creating child elements of the same root concurrently.
     *     I like the idea of queueing {@code createElement} requests, but it's fine the way it is;
     *     there's only contention on the lock when 2 threads are coincidentally working on the same file.
     *     Which happens, so we need this for correctness, but it isn't a performance issue.
     * </p>
     * @param fileIndex file to which the new element is added
     * @param elementPath path of node names to the new element
     * @param elementContents sub-elements, attributes and body to apply to the new element
     * @return a new element
     */
    public Object createElement(final Long fileIndex, final List<QName> elementPath, final XMLGenElement elementContents) {

        final var builder = getDocumentBuilder(fileIndex);
        synchronized (builder) {
            return builder.element(elementPath, elementContents);
        }
    }

    /**
     * Get hold of the root element of the file at the supplied index
     * Create the file on first access, assuming it does not already exist,
     * and build the initial (root) element.
     * <p>
     *     Synchronized to prevent multiple threads potentially creating the same file
     *     when they collide on the file id
     * </p>
     *
     * @param fileIndex index of the XML file being created
     * @return the root element of the XML file for the supplied fileIndex
     */
    synchronized private XMLGenDocBuilder getDocumentBuilder(long fileIndex) {
        return files.computeIfAbsent(fileIndex, newFile -> {
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
                return new XMLGenDocBuilder(pushDocument, pushDocument.element(rootNode));
            } catch (SaxonApiException e) {
                throw new RuntimeException("XML gen file " + file + " could not create serialization.");
            }
        });
    }
}
