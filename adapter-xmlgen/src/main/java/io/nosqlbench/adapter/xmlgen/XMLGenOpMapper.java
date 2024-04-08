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


import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class XMLGenOpMapper implements OpMapper<XMLGenOp> {

    private static final Logger logger = LogManager.getLogger(XMLGenOpMapper.class);

    private final XMLGenDriverAdapter adapter;

    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends XMLGenSpace> spaceCache;

    public XMLGenOpMapper(XMLGenDriverAdapter adapter, NBConfiguration cfg, DriverSpaceCache<? extends XMLGenSpace> spaceCache) {
        this.cfg = cfg;
        this.spaceCache = spaceCache;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends XMLGenOp> apply(ParsedOp op) {

        LongFunction<String> spaceFn = op.getAsFunctionOr("space", "default");
        LongFunction<XMLGenSpace> contextFn = l -> adapter.getSpaceCache()
            .get(spaceFn.apply(l));

        String directoryPath = op.getStaticConfigOr("directorypath", System.getProperty("user.dir"));
        String rootNode = op.getStaticConfigOr("xmlroot", "root");

        spaceCache.get(spaceFn.apply(0L)).createXMLGenContext(directoryPath, rootNode);

        String command = op.getStaticConfigOr("command", "element");

        switch (command) {
            //TODO (AP) define and implement other types of command
            default -> {
                // default is to generate an element
                return new XMLGenOpDispenser(adapter, contextFn, op);
            }
        }
    }


}
