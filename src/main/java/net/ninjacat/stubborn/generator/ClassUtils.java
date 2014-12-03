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

package net.ninjacat.stubborn.generator;

import javassist.ClassPool;
import javassist.CtMember;
import javassist.NotFoundException;
import net.ninjacat.stubborn.exceptions.TransformationException;
import net.ninjacat.stubborn.file.ClassAccessProvider;
import net.ninjacat.stubborn.file.ClassLister;
import net.ninjacat.stubborn.file.ClassPathType;
import net.ninjacat.stubborn.file.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javassist.Modifier.isAbstract;
import static javassist.Modifier.isNative;

public final class ClassUtils {
    private ClassUtils() {
    }

    public static boolean isNonModifiableMethod(CtMember method) {
        int modifiers = method.getModifiers();
        return isAbstract(modifiers) || isNative(modifiers);
    }

    public static void appendClasses(ClassPool injectPool, String path) {
        try {
            injectPool.appendClassPath(path);
        } catch (NotFoundException e) {
            throw new TransformationException("Failed to load source classes from " + path, e);
        }
    }

    public static List<String> getInputClassList(Map<ClassPathType, ClassAccessProvider> providers, Iterable<Source> sources) {
        List<String> classes = new ArrayList<>();
        for (Source source : sources) {
            ClassLister reader = providers.get(source.getType()).getReader(source.getRoot());
            classes.addAll(reader.list());
        }
        // trick to process internal classes first
        classes.sort((o1, o2) -> Boolean.compare(o2.contains("$"), o1.contains("$")));
        return classes;
    }

}
