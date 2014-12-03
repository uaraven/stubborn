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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.ninjacat.stubborn.file.Consts.CLASS_EXT;

public class FsLister implements ClassLister {

    private final String root;

    public FsLister(String root) {
        this.root = root;
    }

    @Override
    public List<String> list() {
        Path path = FileSystems.getDefault().getPath(root);
        try {
            return Files.walk(path).map(Path::toFile).filter(f -> f.isFile() && f.getAbsolutePath().endsWith(CLASS_EXT))
                    .map(f -> convertPathToClassName(root, f.getAbsolutePath())).collect(Collectors.toList());
        } catch (IOException ignored) {
            return Collections.emptyList();
        }
    }

    private static String convertPathToClassName(String root, String path) {
        String cn = path.startsWith(root) ? path.substring(root.length()) : path;
        cn = cn.replaceAll("/", ".");
        return cn.substring(0, cn.length() - CLASS_EXT.length());
    }

}
