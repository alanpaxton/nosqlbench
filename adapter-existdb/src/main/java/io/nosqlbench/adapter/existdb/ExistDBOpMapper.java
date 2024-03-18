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


import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;

public class ExistDBOpMapper implements OpMapper<ExistDBOp> {

    private static final Logger logger = LogManager.getLogger(ExistDBOpMapper.class);

    private final ExistDBDriverAdapter adapter;

    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends ExistDBSpace> spaceCache;

    public ExistDBOpMapper(ExistDBDriverAdapter adapter, NBConfiguration cfg, DriverSpaceCache<? extends ExistDBSpace> spaceCache) {
        this.cfg = cfg;
        this.spaceCache = spaceCache;
        this.adapter = adapter;
    }

    private <T> T getStaticConfigOrWarn(final ParsedOp op, final String name, final T defaultValue, Class<T> tClass) {

        final var supplied = op.getOptionalStaticValue(name, tClass);
        if (supplied.isEmpty()) {
            logger.warn("op field '" + name + "' was not defined. Default being used is '" + defaultValue + "'");
        }
        return supplied.orElse(defaultValue);
    }

    @Override
    public OpDispenser<? extends ExistDBOp> apply(ParsedOp op) {

        LongFunction<String> spaceFn = op.getAsFunctionOr("space", "default");
        LongFunction<ExistDBSpace> contextFn = l -> adapter.getSpaceCache()
            .get(spaceFn.apply(l));

        String connectionURL = op.getStaticConfigOr("endpoint", "unknown");
        if (connectionURL == null) {
            throw new BasicError("Must provide an endpoint value for use by the ExistDB adapter.");
        }

        String collection = getStaticConfigOrWarn(op,"collection", "db", String.class);
        String user = getStaticConfigOrWarn(op, "user", "admin", String.class);
        String pass = getStaticConfigOrWarn(op,"pass", "", String.class);

        spaceCache.get(spaceFn.apply(0L)).createExistDBClient(connectionURL, collection, user, pass);

        String command = op.getStaticConfigOr("command", "query");

        var stmt = op.getAsRequiredFunction("stmt", Object.class);

        switch (command) {
            //TODO (AP) define and implement other types of command
            case "generate":
                return new ExistDBGeneratorOpDispenser(adapter, contextFn, op);
            case "query":
            default:
                // default is to submit an xquery "query"
                return new ExistDBQueryOpDispenser(adapter, contextFn, op);

        }

    }
}

