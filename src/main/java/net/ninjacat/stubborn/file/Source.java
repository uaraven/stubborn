/*
 * Copyright 2014 Oleksiy Voronin <ovoronin@gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.ninjacat.stubborn.file;

import net.ninjacat.stubborn.generator.Context;

import java.io.IOException;
import java.util.zip.ZipFile;

import static net.ninjacat.stubborn.file.ClassPathType.Folder;
import static net.ninjacat.stubborn.file.ClassPathType.Jar;

public class Source {

    private static final String JAR = ".jar";

    private final ClassPathType type;
    private final String root;

    public Source(Context context, String root) {
        this.root = root;
        this.type = isZipFile(root) ? Jar : Folder;
    }

    public ClassPathType getType() {
        return type;
    }

    public String getRoot() {
        return root;
    }

    private boolean isZipFile(String fileName) {
        try {
            new ZipFile(fileName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
