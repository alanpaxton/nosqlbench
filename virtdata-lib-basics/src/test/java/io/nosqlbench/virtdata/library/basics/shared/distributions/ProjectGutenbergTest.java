package io.nosqlbench.virtdata.library.basics.shared.distributions;

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


import io.nosqlbench.virtdata.library.basics.shared.unary_int.Hash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectGutenbergTest {

    private final static Logger logger = LogManager.getLogger(ProjectGutenbergTest.class);

    @Test
    public void mobyDickSample() {
        final var hash = new Hash();
        final ProjectGutenberg moby = new ProjectGutenberg("data/gutenberg/pg2701mini.txt");
        assertThat(moby.apply(hash.applyAsInt(3))).startsWith(
            "Caught and twisted—corkscrewed in the mazes of the line, loose harpoons");
        assertThat(moby.apply(hash.applyAsInt(4))).startsWith(
            "In the Propontis, as far as I can learn, none of that peculiar");
        assertThat(moby.apply(hash.applyAsInt(5))).startsWith(
            "When the entire ship’s company were assembled, and with curious and not");
        assertThat(moby.apply(hash.applyAsInt(6))).startsWith(
            "Doubtless one leading reason why the world declines honoring us\nwhalemen, is this");
    }
}
