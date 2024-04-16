package io.nosqlbench.adapter.xmlgen;

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

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.push.Element;

import java.util.HashMap;
import java.util.List;

/**
 * Keep multiple elements open for update
 */
class OpenElementTree {

    Element element;

    HashMap<QName, OpenElementTree> children;

    OpenElementTree(final Element element) {
        this.element = element;
        this.children = HashMap.newHashMap(2);
    }

    /**
     * Close the element on the path, and all its children
     *
     * @param path
     */
    void close(List<QName> path) {
        if (path.isEmpty()) {
            for (var child : children.values()) {
                child.close(path);
            }
            try {
                element.close();
            } catch (SaxonApiException e) {
                throw new RuntimeException("Error closing element: " + element, e);
            }
            children.clear();
        } else {
            var key = path.getFirst();
            var subTree = children.get(key);
            if (subTree != null) {
                subTree.close(path.subList(1, path.size()));
            }
            if (path.size() == 1) {
                children.remove(key);
            }
        }
    }

    /**
     * Close the entire tree
     */
    void close() {
        close(List.of());
    }

    /**
     * Open a path, creating subtree(s) as necessary
     *
     * @param path to open
     * @return the element on the path
     */
    Element open(List<QName> path) {
        if (path.isEmpty()) {
            return element;
        }
        var key = path.getFirst();
        var subTree = children.get(key);
        if (subTree == null) {
            Element keyElement = null;
            try {
                keyElement = element.element(key);
            } catch (SaxonApiException e) {
                throw new RuntimeException("Error creating element " + key + " " + e.getMessage(), e);
            }
            subTree = new OpenElementTree(keyElement);
            children.put(key, subTree);
        }
        return subTree.open(path.subList(1, path.size()));
    }

    Element replace(List<QName> path) {
        close(path);
        return open(path);
    }
}
