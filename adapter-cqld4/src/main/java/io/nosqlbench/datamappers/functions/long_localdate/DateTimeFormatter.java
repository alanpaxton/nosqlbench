package io.nosqlbench.datamappers.functions.long_localdate;

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

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.time.LocalDate;
import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.datetime})
public class DateTimeFormatter implements Function<LocalDate, String> {

    private final java.time.format.DateTimeFormatter formatter;

    @Example({"DateTimeFormatter(fmt)","format a LocalDate based on the supplied format"})
    public DateTimeFormatter(String format){
        this.formatter = java.time.format.DateTimeFormatter.ofPattern(format);
    }

    @Override
    public String apply(LocalDate localDate) {
        return localDate.format(formatter);
    }
}
