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

import net.ninjacat.stubborn.exceptions.TransformationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FsWriter implements Writer {

    private final String root;

    public FsWriter(String root) {
        this.root = root;
    }

    @Override
    public void addClass(String canonicalName, byte[] classData) {
        String path = root + File.separator + canonicalName.replaceAll("\\.", "/") + Consts.CLASS_EXT;
        File targetFile = new File(path);
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream output = new FileOutputStream(targetFile)) {
            output.write(classData);
        } catch (IOException e) {
            throw new TransformationException("Failed to write resulting class file " + targetFile, e);
        }
    }

    @Override
    public void close() {
        // do nothing
    }
}
