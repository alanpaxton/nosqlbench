/*
 * Copyright (c) 2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.activityconfig.yaml;

import java.nio.file.Path;

public enum TemplateFormat {
    yaml("yaml","yml"),
    json("json","json5"),
    jsonnet("jsonnet","jsonnet5"),
    inline();

    private final String[] pathExtensions;

    TemplateFormat(String... pathExtensions) {
        this.pathExtensions = pathExtensions;
    }

    public static TemplateFormat valueOfPath(Path path) {
        var fullName = path.toString();
        String extension = fullName.substring(fullName.lastIndexOf('.')).toLowerCase();

        for (TemplateFormat value : values()) {
            for (String pathExtension : value.pathExtensions) {
                if (pathExtension.equals(extension)) {
                    return value;
                }
            }
        }
        throw new RuntimeException("Unable to determine source format for " + path);
    }
}
