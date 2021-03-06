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

package net.ninjacat.stubborn.generator.rules;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import javassist.CtMethod;
import net.ninjacat.stubborn.exceptions.TransformationException;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@XStreamAlias("rules")
public class TransformRules {
    @XStreamImplicit(itemFieldName = "inject")
    private final List<InjectRule> injectRules;
    @XStreamAlias("methods")
    private final List<MethodMatcher> matchers;
    @XStreamImplicit(itemFieldName = "strip-class")
    private final List<String> stripClasses;
    @XStreamImplicit(itemFieldName = "skip-class")
    private final List<String> skipClasses;

    private List<Pattern> stripPatterns;
    private List<Pattern> skipPatterns;

    private TransformRules() {
        matchers = new ArrayList<>();
        injectRules = new ArrayList<>();
        stripClasses = new ArrayList<>();
        skipClasses = new ArrayList<>();
    }

    public static TransformRules loadFromStream(InputStream inputStream) {
        XStream stream = new XStream();
        stream.processAnnotations(new Class[]{TransformRules.class, MethodMatcher.class, InjectRule.class});
        return verify((TransformRules) stream.fromXML(inputStream));
    }

    public boolean shouldStripClass(CharSequence className) {
        if (stripPatterns == null) {
            loadStripPatterns();
        }
        for (Pattern pattern : stripPatterns) {
            if (pattern.matcher(className).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldSkipClass(CharSequence className) {
        if (skipPatterns == null) {
            loadSkipPatterns();
        }
        for (Pattern pattern : skipPatterns) {
            if (pattern.matcher(className).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasInjectRules() {
        return injectRules != null && !injectRules.isEmpty();
    }

    public List<InjectRule> getInjectRules() {
        return Collections.unmodifiableList(injectRules);
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

    private static TransformRules verify(TransformRules transformRules) {
        for (MethodMatcher mm : transformRules.matchers) {
            if (mm.isMissingConditions()) {
                throw new TransformationException("Matcher with no conditions: " + mm);
            }
        }
        return transformRules;
    }

    private static void compilePatterns(Iterable<String> regexps, Collection<Pattern> result) {
        //noinspection ConstantConditions
        if (regexps == null) {
            return;
        }
        for (String re : regexps) {
            result.add(Pattern.compile(re));
        }
    }

    private void loadStripPatterns() {
        stripPatterns = new ArrayList<>();
        compilePatterns(stripClasses, stripPatterns);
    }

    private void loadSkipPatterns() {
        skipPatterns = new ArrayList<>();
        compilePatterns(skipClasses, skipPatterns);
    }
}
