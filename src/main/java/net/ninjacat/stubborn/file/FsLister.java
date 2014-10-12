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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.ninjacat.stubborn.file.Consts.CLASS_EXT;

public class FsLister implements ClassLister {

    private final String root;

    public FsLister(String root) {
        this.root = root;
    }

    private static String convertPathToClassName(String root, String path) {
        String cn = path.startsWith(root) ? path.substring(root.length() + 1) : path;
        cn = cn.replaceAll("/", ".");
        return cn.substring(0, cn.length() - CLASS_EXT.length());
    }

    @Override
    public List<String> list() {
        List<String> result = new ArrayList<>();
        readClasses(new File(root), result);
        result = result.parallelStream().map(p -> convertPathToClassName(root, p)).collect(Collectors.toList());
        return result;
    }

    private void readClasses(File root, List<String> result) {
        File[] fileList = root.listFiles();
        if (fileList != null) {
            List<File> files = Arrays.asList(fileList);
            files.stream().filter(File::isFile).filter(f -> f.getName().endsWith(CLASS_EXT)).forEach(f -> result.add(f.getAbsolutePath()));
            files.stream().filter(File::isDirectory).forEach(f -> readClasses(f.getAbsoluteFile(), result));
        }
    }

}
