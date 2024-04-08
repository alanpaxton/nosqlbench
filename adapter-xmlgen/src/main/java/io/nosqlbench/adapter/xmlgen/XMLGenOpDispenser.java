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

import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class XMLGenOpDispenser extends BaseOpDispenser<XMLGenOp, XMLGenSpace>  {

    private final LongFunction<XMLGenOp> opFunc;

    public XMLGenOpDispenser(XMLGenDriverAdapter adapter, LongFunction<XMLGenSpace> contextFn, ParsedOp op) {
        super(adapter, op);
        opFunc = createOpFunc(contextFn, op);
    }

    private LongFunction<XMLGenOp> createOpFunc(LongFunction<XMLGenSpace> contextFn, ParsedOp op) {

        LongFunction<?> content = op.getAsRequiredFunction("stmt", Object.class);

        var children = op.getAsRequiredFunction("children", Map.class);
        LongFunction<Map<String, Object>> attrs = op.getAsFunctionOr("attrs", Map.of());
        var file = op.getAsRequiredFunction("file", Long.class);
        var body = op.getAsFunctionOr("body", "");

        var path = op.getAsRequiredFunction("path", List.class);

        return l -> new XMLGenOp(
            contextFn.apply(l).getXMLGenContext(),
            content.apply(l),
            file.apply(l),
            path.apply(l),
            children.apply(l),
            attrs.apply(l),
            body.apply(l));
    }

    @Override
    public XMLGenOp apply(long value) {
        return opFunc.apply(value);
    }
}
