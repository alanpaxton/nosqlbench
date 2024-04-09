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

import io.nosqlbench.adapter.xmlgen.XMLGenDocBuilder;
import io.nosqlbench.adapter.xmlgen.XMLGenElement;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class XMLGenDocBuilderTest {

    @TempDir
    static Path tempDir;
    static Path tempFile;

    @BeforeAll
    public static void init() throws IOException {
        tempFile = Files.createFile(tempDir.resolve("test.txt"));
    }

    @Test public void replaceSamePath() throws SaxonApiException {

        final var rootNode = "root";
        final var processor = new Processor();
        final var serializer = processor.newSerializer(tempFile.toFile());
        serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
        final var pushDocument = processor.newPush(serializer).document(true);
        var builder = new XMLGenDocBuilder(pushDocument, pushDocument.element(rootNode));
        builder.element(List.of(new QName("people"), new QName("person")),
            new XMLGenElement(Map.of(), Map.of(), "body1"));
        builder.element(List.of(new QName("one"), new QName("two"), new QName("three")),
            new XMLGenElement(Map.of(), Map.of(), "body2"));
    }
}
