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

package io.nosqlbench.engine.extensions.computefunctions.relavency;

import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.engine.extensions.computefunctions.ComputeFunctions;

import java.util.Map;

public class F1 extends BaseRelevancyFunction {
    private final int k;

    public F1(String name, int k, Map<String,String> labels) {
        super(name, NBLabels.forKV("k",labels).andTypes(labels));
        this.k = k;
    }
    public F1(String name, int k, Object... labels) {
        super(name, NBLabels.forKV("k",k).andTypes(labels));
        this.k = k;
    }

    @Override
    public double apply(int[] relevant, int[] actual) {
        return ComputeFunctions.F1(relevant,actual,k);
    }

}
