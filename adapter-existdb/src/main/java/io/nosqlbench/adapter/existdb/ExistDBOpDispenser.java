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

import java.util.function.LongFunction;

public class ExistDBOpDispenser extends BaseOpDispenser<ExistDBOp, ExistDBSpace> {

    private final LongFunction<ExistDBOp> opFunc;

    public ExistDBOpDispenser(ExistDBDriverAdapter adapter, LongFunction<ExistDBSpace> ctxF, ParsedOp op) {
        super(adapter, op);
        opFunc = getOpFunc(ctxF, op);
    }

    private LongFunction<ExistDBOp> getOpFunc(LongFunction<ExistDBSpace> ctxF, ParsedOp op) {

        return null;
    }

    @Override
    public ExistDBOp apply(long value) {
        return null;
    }
}
