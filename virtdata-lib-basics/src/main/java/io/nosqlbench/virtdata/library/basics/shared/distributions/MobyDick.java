package io.nosqlbench.virtdata.library.basics.shared.distributions;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

@ThreadSafeMapper
@Categories(Category.premade)
public class MobyDick extends ProjectGutenberg {

    public MobyDick() {
        super("data/gutenberg/pg2701.txt");
    }
}
