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


import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class XMLGenOp implements CycleOp<Object> {

    private final static Logger logger = LogManager.getLogger(XMLGenSpace.class);

    @Override
    public String toString() {
        return "XMLGenOp{" +
            "definition=" + definition +
            ", attrs=" + attrs +
            ", file(index)=" + file +
            ", element='" + element + '\'' +
            ", body=" + body +
            '}';
    }

    final XMLGenContext xmlGenContext;

    final Object definition;
    final Map attrs;
    final Long file;
    final String element;
    final String body;

    public XMLGenOp(final XMLGenContext xmlGenContext, final Object definition, final Long file, final String element, final Map<String, Object> attrs, final String body) {
        this.xmlGenContext = xmlGenContext;
        this.definition = definition;
        this.attrs = attrs;
        this.file = file;
        this.element = element;
        this.body = body;
    }

    @Override
    public Object apply(long value) {
        return xmlGenContext.createElement(file, element, attrs, body);
    }

}
