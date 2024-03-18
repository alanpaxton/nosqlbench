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


import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "existdb")
public class ExistDBDriverAdapter extends BaseDriverAdapter<ExistDBOp, ExistDBSpace> {

    public ExistDBDriverAdapter(NBComponent parentComponent, NBLabels labels) {
        super(parentComponent, labels);
    }

    @Override
    public OpMapper<ExistDBOp> getOpMapper() {
        DriverSpaceCache<? extends ExistDBSpace> spaceCache = getSpaceCache();
        NBConfiguration config = getConfiguration();
        return new ExistDBOpMapper(this, config, spaceCache);
    }

    @Override
    public Function<String, ? extends ExistDBSpace> getSpaceInitializer(NBConfiguration cfg) {
        return spaceName -> new ExistDBSpace(spaceName, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(ExistDBSpace.getConfigModel());
    }

}