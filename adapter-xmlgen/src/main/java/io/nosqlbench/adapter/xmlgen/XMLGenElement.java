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

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The element to add to generated XML
 *
 * @param children
 * @param attrs
 * @param body
 */
public record XMLGenElement(Map<String, Object> children, Map<String, Object> attrs, String body) {
    public XMLGenElement substitute(final List<Pair<String, Object>> substitutions) {
        var body = substitute(this.body, substitutions);
        var children = substitute(this.children, substitutions);
        var attrs = substitute(this.attrs, substitutions);
        return new XMLGenElement(children, attrs, body);
    }

    private static String substitute(final String target, List<Pair<String, Object>> substitutions) {
        var result = target;
        for (var sub : substitutions) {
            result = result.replace(sub.getLeft(), sub.getRight().toString());
        }
        return result;
    }

    private static List<Object> substitute(List<Object> target, List<Pair<String, Object>> substitutions) {
        var result = new ArrayList<>(target.size());
        for (var value : target) {
            if (value instanceof Map<?,?> valueMap) {
                result.add(substitute((Map<String, Object>) valueMap, substitutions));
            } else if (value instanceof String valueString) {
                result.add(substitute(valueString, substitutions));
            } else if (value instanceof List<?> valueList) {
                result.add(substitute((List<Object>) valueList, substitutions));
            } else {
                //Any other kind of object, terminate substitution
                result.add(value);
            }
        }

        return result;
    }

    private static Map<String, Object> substitute(Map<String, Object> target, List<Pair<String, Object>> substitutions) {
        var result = new HashMap<String, Object>(target.size());
        for (var item : target.entrySet()) {
            var value = item.getValue();
            if (value instanceof Map<?,?> valueMap) {
                result.put(item.getKey(), substitute((Map<String, Object>) valueMap, substitutions));
            } else if (value instanceof String valueString) {
                result.put(item.getKey(), substitute(valueString, substitutions));
            } else if (value instanceof List<?> valueList) {
                result.put(item.getKey(), substitute((List<Object>) valueList, substitutions));
            } else {
                //Any other kind of object, terminate substitution
                result.put(item.getKey(), value);
            }
        }

        return result;
    }
}
