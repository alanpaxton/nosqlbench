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


import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

public class ExistDBOp implements CycleOp<Object> {

    private final ExistDBClient client;

    private final String collection;

    private final String xquery;

    public ExistDBOp(ExistDBClient client, String collection, String xquery) {
        this.client = client;
        this.collection = collection;
        this.xquery = xquery;
    }

    @Override
    public Object apply(long value) {
        return client.executeQuery(xquery);
    }
}
