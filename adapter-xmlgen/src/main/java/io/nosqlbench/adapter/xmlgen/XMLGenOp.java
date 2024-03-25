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
import net.sf.saxon.s9api.QName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class XMLGenOp implements CycleOp<Object> {

    private final static Logger logger = LogManager.getLogger(XMLGenSpace.class);

    @Override
    public String toString() {
        return "XMLGenOp{" +
            "definition=" + definition +
            ", children=" + element.children() +
            ", attrs=" + element.attrs() +
            ", file(index)=" + file +
            ", path='" + path + '\'' +
            ", body=" + element.body() +
            '}';
    }

    final XMLGenContext xmlGenContext;

    final Object definition;
    final Long file;
    final List<QName> path;
    final XMLGenElement element;

    public XMLGenOp(final XMLGenContext xmlGenContext, final Object definition, final Long file, final List<String> path, final Map<String, Object> children, final Map<String, Object> attrs, final String body) {
        this.xmlGenContext = xmlGenContext;
        this.definition = definition;
        this.element = new XMLGenElement(children, attrs, body);
        this.file = file;
        this.path = path.stream().map(name -> new QName(name)).toList();
    }

    @Override
    public Object apply(long value) {
        return xmlGenContext.createElement(file, path, element);
    }

}
