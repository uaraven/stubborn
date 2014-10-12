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

package net.ninjacat.stubborn.generator.rules;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import javassist.CtMethod;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@XStreamAlias("rules")
public class Matchers {
    @XStreamAlias("methods")
    private final List<MethodMatcher> matchers;

    public Matchers() {
        this.matchers = new ArrayList<>();
    }

    public static Matchers loadFromString(String rules) {
        XStream stream = new XStream();
        stream.processAnnotations(new Class[]{Matchers.class, MethodMatcher.class});
        return (Matchers) stream.fromXML(rules);
    }

    public static Matchers loadFromFile(String rulesFile) throws FileNotFoundException {
        return loadFromStream(new FileInputStream(rulesFile));
    }

    public static Matchers loadFromStream(InputStream inputStream) {
        XStream stream = new XStream();
        stream.processAnnotations(new Class[]{Matchers.class, MethodMatcher.class});
        return (Matchers) stream.fromXML(inputStream);
    }

    public Optional<MethodMatcher> findMatcher(CtMethod method, boolean ignoreDuplicates) {
        List<MethodMatcher> matchingList = matchers.stream().filter(m -> m.isMatching(method)).collect(Collectors.toList());
        if (matchingList.size() > 1 && !ignoreDuplicates) {
            String conflicts = matchingList.stream().map(MethodMatcher::repr).reduce(String::concat).get();
            throw new IllegalStateException("More than one matcher for a method found:\n" + conflicts);
        }
        if (matchingList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(matchingList.get(0));
        }
    }
}
