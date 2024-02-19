package io.nosqlbench.adapter.existdb;

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
import org.exist.xmldb.DatabaseImpl;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

public class ExistDBSpace implements NBNamedElement, AutoCloseable {

    private final static Logger logger = LogManager.getLogger(ExistDBSpace.class);
    private final String spaceName;
    private final NBConfiguration existDBConfig;
    private ExistDBClient existDBClient;

    public ExistDBSpace(String name, NBConfiguration cfg) {
        this.spaceName = name;
        this.existDBConfig = cfg;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(ExistDBSpace.class)
            .add(Param.required("endpoint", String.class)
                .setDescription("The connection endpoint for the eXist-db database"))
            .asReadOnly();

    }

    @Override
    public String getName() {
        return spaceName;
    }

    @Override
    public void close() {
        try {
            if (existDBClient != null) {
                existDBClient.close();
            }
        } catch (Exception e) {
            logger.error(() -> "auto-closeable eXist-db connection threw exception in " +
                "eXist-db space(" + this.spaceName + "): " + e);
            throw new RuntimeException(e);
        }
    }

    public void createExistDBClient(String connectionURL, String collection) {

        this.existDBClient = new ExistDBClient(connectionURL, collection);
    }

    public ExistDBClient getClient() {
        return this.existDBClient;
    }
}
