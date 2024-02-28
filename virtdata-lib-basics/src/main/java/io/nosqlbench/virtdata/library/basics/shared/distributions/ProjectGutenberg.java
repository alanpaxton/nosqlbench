package io.nosqlbench.virtdata.library.basics.shared.distributions;

import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasElementSampler;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.ElemProbD;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
@Categories(Category.premade)
public class ProjectGutenberg implements LongFunction<String> {

    // Hold indexes into paragraphs in the sampler
    private final AliasElementSampler<Long> sampler;
    private final String[] paragraphs;

    public ProjectGutenberg(String... data) {


        final List<String> collectParagraphs = new ArrayList<>();

        for (String filename : data) {
            final ParagraphBuilder paragraphBuilder = new ParagraphBuilder();
            for (var line : NBIO.readLines(filename)) {
                paragraphBuilder.addLine(line);
            }
            collectParagraphs.addAll(paragraphBuilder.getParagraphs());
        }

        paragraphs = collectParagraphs.toArray(new String[0]);
        final var probabilities = new ArrayList<ElemProbD<Long>>(collectParagraphs.size());
        for (int i = 0; i < collectParagraphs.size(); i++) {
            probabilities.add(new ElemProbD<>((long)i, 1.0));
        }
        sampler = new AliasElementSampler<>(probabilities);
   }

    static final LongUnaryOperator prefunc = new Hash();

    @Override
    public String apply(long value) {

        double unitValue = (double) prefunc.applyAsLong(value) / (double) Long.MAX_VALUE;
        return paragraphs[Math.toIntExact(sampler.apply(unitValue))];
    }

    static private class ParagraphBuilder {

        enum Position {
            Before,
            Body,
            After
        };

        private Position position = Position.Before;

        private final static String CHAPTER_PREFIX = "CHAPTER";
        private final static String EPILOGUE_PREFIX = "Epilogue";

        private final List<String> paragraphs = new ArrayList<>();

        private final StringBuilder current = new StringBuilder();

        final void addLine(final String rawLine) {
            final String line = rawLine.strip();
            switch (position) {
                case Body -> {
                    if (line.startsWith(CHAPTER_PREFIX) || line.isEmpty()) {
                        if (!current.isEmpty()) {
                            paragraphs.add(current.toString());
                        }
                        current.setLength(0);
                    } else if (line.startsWith(EPILOGUE_PREFIX)) {
                        paragraphs.add(current.toString());
                        // ignore everything after the epilogue
                        current.setLength(0);
                        position = Position.After;
                    } else {
                        if (!current.isEmpty()) {
                            current.append('\n');
                        }
                        current.append(line);
                    }
                }
                case Before, After -> {
                    // Because we may think we are in Epilogue, but it was just the table-of-contents
                    if (line.startsWith(CHAPTER_PREFIX)) {
                        position = Position.Body;
                        current.setLength(0);
                    }
                }
            }
        }

        final List<String> getParagraphs() {
            return new ArrayList<>(paragraphs);
        }
    }
}
