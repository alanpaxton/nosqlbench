package io.nosqlbench.adapter.xmlgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

enum Keyword {
    CHILDREN("children"),
    ATTRS("attrs"),
    BODY("body"),
    STMT("stmt"),
    PATH("path"),
    FILE("file"),

    FOREACH("foreach");

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
