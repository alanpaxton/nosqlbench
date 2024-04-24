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


import io.nosqlbench.nb.api.components.core.NBNamedElement;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XMLGenSpace implements NBNamedElement, AutoCloseable {

    private final static Logger logger = LogManager.getLogger(XMLGenSpace.class);
    private final String spaceName;
    private final NBConfiguration xmlGenConfig;

    private XMLGenContext xmlGenContext;

    public XMLGenSpace(String name, NBConfiguration cfg) {
        this.spaceName = name;
        this.xmlGenConfig = cfg;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(XMLGenSpace.class)
            .add(Param.required("directorypath", String.class)
                .setDescription("The path at which to create the files"))
                .add(Param.required("xmlroot", String.class)
                .setDescription("The single root element of all files"))
            .asReadOnly();
    }

    @Override
    public String getName() {
        return spaceName;
    }

    @Override
    public void close() {
        try {
            if (xmlGenContext != null) {
                xmlGenContext.close();
            }
        } catch (Exception e) {
            logger.error(() -> "auto-closeable xmlgen generation context threw exception in " +
                "xmlgen space(" + this.spaceName + "): " + e);
            throw new RuntimeException(e);
        }
    }



    public void createXMLGenContext(final String directorypath, final String rootNode) {

        synchronized (this) {
            if (xmlGenContext == null) {
                xmlGenContext = new XMLGenContext(directorypath, rootNode);
            }
        }
    }

    public XMLGenContext getXMLGenContext() {
        return this.xmlGenContext;
    }

}

