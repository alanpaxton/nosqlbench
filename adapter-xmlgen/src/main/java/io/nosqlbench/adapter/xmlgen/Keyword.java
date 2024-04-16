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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

enum Keyword {
    CHILDREN("__children"),
    ATTRS("__attrs"),
    BODY("__body"),
    STMT("stmt"),
    PATH("path"),
    FILE("file"),

    FOREACH("__foreach");

    private static final Map<String, Keyword> LabelMap = new HashMap<>();

    static {
        for (Keyword k : values()) {
            LabelMap.put(k.label, k);
        }
    }

    public final String label;

    private Keyword(final String label) {
        this.label = label;
    }

    public static Optional<Keyword> ValueOfLabel(final String label) {
        return Optional.of(LabelMap.get(label));
    }

    public static boolean IsLabel(final String label) {
        return LabelMap.containsKey(label);
    }
}
