/*
 * Copyright 2014 Oleksiy Voronin <ovoronin@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ninjacat.stubborn.file;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.ninjacat.stubborn.file.Consts.CLASS_EXT;

public class JarLister implements ClassLister {
    private final String jarFile;

    public JarLister(String jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public List<String> list() {
        try {
            Collection<String> results = new ArrayList<>();
            ZipFile file = new ZipFile(jarFile);
            asIterator(file.entries()).forEachRemaining(e -> {
                if (e.getName().endsWith(CLASS_EXT)) {
                    results.add(e.getName());
                }
            });
            return results.parallelStream().map(JarLister::convertPathToClassName).collect(Collectors.toList());
        } catch (IOException ignored) {
            return Collections.emptyList();
        }
    }

    private static Iterator<ZipEntry> asIterator(Enumeration<? extends ZipEntry> entries) {
        return new Iterator<ZipEntry>() {
            @Override
            public boolean hasNext() {
                return entries.hasMoreElements();
            }

            @Override
            public ZipEntry next() {
                return entries.nextElement();
            }
        };
    }

    private static String convertPathToClassName(String p) {
        return p.substring(0, p.length() - CLASS_EXT.length()).replaceAll("/", ".");
    }

}
