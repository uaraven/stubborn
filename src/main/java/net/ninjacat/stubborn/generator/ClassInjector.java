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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import net.ninjacat.stubborn.file.ClassAccessProvider;
import net.ninjacat.stubborn.file.ClassPathType;
import net.ninjacat.stubborn.file.Source;
import net.ninjacat.stubborn.file.Writer;
import net.ninjacat.stubborn.generator.rules.InjectRule;
import net.ninjacat.stubborn.log.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.ninjacat.stubborn.generator.ClassUtils.appendClasses;
import static net.ninjacat.stubborn.log.LogLevel.Noisy;

public class ClassInjector {

    private final Map<ClassPathType, ClassAccessProvider> providers;
    private final Logger logger;

    @Inject
    public ClassInjector(Map<ClassPathType, ClassAccessProvider> providers, Logger logger) {
        this.providers = Collections.unmodifiableMap(providers);
        this.logger = logger;
    }

    public void injectClasses(Writer writer, Iterable<InjectRule> injectRules) {
        for (InjectRule rule : injectRules) {
            processInjectRule(rule, writer);
        }
    }

    private static ClassPool buildClassPool(InjectRule injectRule) {
        ClassPool injectPool = new ClassPool(false);
        appendClasses(injectPool, injectRule.getPath());
        injectPool.appendSystemPath();
        return injectPool;
    }

    private static boolean isMatching(CharSequence className, Stream<Pattern> classes) {
        return classes.anyMatch(p -> p.matcher(className).matches());
    }

    private void processInjectRule(InjectRule rule, Writer writer) {
        ClassPool injectPool = buildClassPool(rule);
        List<String> classes = buildClassList(rule);

        List<Pattern> patterns = rule.getClasses().stream().map(Pattern::compile).collect(Collectors.toList());

        classes.stream().filter(className -> isMatching(className, patterns.stream())).forEach(className -> {
            try {
                logger.log(Noisy, "Injecting class %s", className);
                CtClass cls = injectPool.get(className);
                writer.addClass(cls.getName(), cls.toBytecode());
            } catch (NotFoundException | CannotCompileException | IOException ex) {
                logger.err(ex, "Failed to inject class %s", className);
            }
        });
    }

    private List<String> buildClassList(InjectRule injectRule) {
        Collection<Source> sources = new ArrayList<>();
        sources.add(new Source(injectRule.getPath()));
        return ClassUtils.getInputClassList(providers, sources);
    }

}
