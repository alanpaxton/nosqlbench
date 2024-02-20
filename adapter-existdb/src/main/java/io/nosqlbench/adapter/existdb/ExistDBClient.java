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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.xmldb.DatabaseImpl;
import org.exist.xmldb.EXistResource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XQueryService;

import java.util.ArrayList;
import java.util.List;

public class ExistDBClient {

    private final static Logger logger = LogManager.getLogger(ExistDBClient.class);

    private boolean libraryIsLoaded = false;

    private final String closeErrorMessage;

    Collection xmldbCollection;
    XQueryService xQueryService;

    private synchronized void loadLibrary() {

        if (!libraryIsLoaded) {

            final String driver = "org.exist.xmldb.DatabaseImpl";
            try {
                Class<DatabaseImpl> driverClass = (Class<DatabaseImpl>) Class.forName(driver);
                Database database = driverClass.getDeclaredConstructor().newInstance();
                database.setProperty("create-database", "false");
                DatabaseManager.registerDatabase(database);

                libraryIsLoaded = true;
            } catch (Exception e) {
                logger.error(() -> "Loading eXist-db driver threw exception " + e);
                throw new RuntimeException(e);
            }
        }

    }

    public List<Object> executeQuery(final String query) {

        try {
            CompiledExpression compiled = xQueryService.compile(query);
            ResourceSet result = xQueryService.execute(compiled);
            ResourceIterator i = result.getIterator();
            Resource res = null;

            final List<Object> eager = new ArrayList<>();
            while (i.hasMoreResources()) {
                try {
                    res = i.nextResource();
                    eager.add(res.getContent());
                } finally {
                    if (res != null) {
                        ((EXistResource) res).freeResources();
                    }
                }
            }
            return eager;
        } catch (XMLDBException e) {
            logger.error(() -> "eXist-db connection threw exception in query (" +
                query + "): " + e);
            throw new RuntimeException(e);
        }

    }

    public ExistDBClient(String connectionURL, String collection, String user, String pass) {
        loadLibrary();

        closeErrorMessage = "eXist-db connection threw exception connection (" +
            connectionURL + ") and collection (" + collection + ")";

        try {
            xmldbCollection = DatabaseManager.getCollection(connectionURL + "/" + collection);
            xQueryService = (XQueryService) xmldbCollection.getService("XQueryService", "1.0");
            xQueryService.setProperty("indent", "yes");

        } catch (XMLDBException e) {
            logger.error(() -> "create connection; " + closeErrorMessage + " : " + e);
            close();
            throw new RuntimeException(e);
        }
    }

    public void close() {

        if (xmldbCollection != null) {
            try {
                xmldbCollection.close();
            } catch (XMLDBException e) {
                logger.error(() -> "close() connection; " + closeErrorMessage + " : " + e);
                throw new RuntimeException(e);
            }
        }
    }

}
