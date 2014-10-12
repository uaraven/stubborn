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

import net.ninjacat.stubborn.exceptions.TransformationException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JarWriter implements Writer {

    private final ZipOutputStream jarFile;

    public JarWriter(String jarFile) {
        try {
            this.jarFile = new ZipOutputStream(new FileOutputStream(jarFile));

            writeManifest();
        } catch (IOException e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public void addClass(String canonicalName, byte[] classData) {
        String path = canonicalName.replaceAll("\\.", "/") + Consts.CLASS_EXT;
        ZipEntry entry = new ZipEntry(path);
        entry.setMethod(ZipEntry.DEFLATED);
        try {
            jarFile.putNextEntry(entry);
            jarFile.write(classData, 0, classData.length);
            jarFile.closeEntry();
        } catch (IOException e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public void close() {
        try {
            jarFile.close();
        } catch (IOException e) {
            throw new TransformationException(e);
        }
    }

    private void writeManifest() throws IOException {
        ZipEntry manifest = new ZipEntry("META-INF/MANIFEST.MF");
        byte[] manifestText = "Manifest-Version: 1.0\nCreated-By: Stubborn\n".getBytes(Charset.forName("UTF-8"));
        jarFile.putNextEntry(manifest);
        jarFile.write(manifestText, 0, manifestText.length);
        jarFile.closeEntry();
    }
}
