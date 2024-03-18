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


import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.LongFunction;

public class ExistDBGeneratorOpDispenser extends BaseOpDispenser<ExistDBOp, ExistDBSpace> {

    private final static Logger logger = LogManager.getLogger(ExistDBSpace.class);

    private final LongFunction<ExistDBGeneratorOp> opFunc;

    public ExistDBGeneratorOpDispenser(ExistDBDriverAdapter adapter, LongFunction<ExistDBSpace> contextFn, ParsedOp op) {
        super(adapter, op);
        opFunc = createOpFunc(contextFn, op);
    }

    private LongFunction<ExistDBGeneratorOp> createOpFunc(LongFunction<ExistDBSpace> contextFn, ParsedOp op) {

        final LongFunction<String> collectionFn = op.getAsFunctionOr("collection", "db");

        LongFunction<?> content = op.getAsRequiredFunction("stmt", Object.class);

        var attrs = op.getAsRequiredFunction("attrs", Map.class);
        var fullname = op.getAsRequiredFunction("fullname", String.class);
        var city = op.getAsRequiredFunction("city", Object.class);
        var body = op.getAsRequiredFunction("body", Object.class);

        return l -> new ExistDBGeneratorOp(
            contextFn.apply(l).getClient(),
            collectionFn.apply(l),
            content.apply(l),
            attrs.apply(l),
            fullname.apply(l),
            city.apply(l),
            body.apply(l));
    }

    @Override
    public ExistDBGeneratorOp apply(long value) {
        final var result = opFunc.apply(value);
        logger.warn(() -> result);

        return result;
    }
}

