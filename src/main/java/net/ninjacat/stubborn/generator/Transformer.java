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

package net.ninjacat.stubborn.generator;

import javassist.*;
import javassist.runtime.Desc;
import net.ninjacat.stubborn.exceptions.TransformationException;
import net.ninjacat.stubborn.file.*;
import net.ninjacat.stubborn.generator.rules.Matchers;
import net.ninjacat.stubborn.generator.rules.MethodMatcher;
import net.ninjacat.stubborn.generator.rules.RulesProvider;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javassist.Modifier.FINAL;
import static javassist.Modifier.PUBLIC;
import static net.ninjacat.stubborn.generator.LogLevel.*;

public class Transformer {

    private final Map<ClassPathType, ClassAccessProvider> providers;
    private final RulesProvider rulesProvider;
    private final BodyGenerator bodyGenerator;
    private final Logger logger;

    @Inject
    public Transformer(Map<ClassPathType, ClassAccessProvider> providers,
                       RulesProvider rulesProvider,
                       BodyGenerator bodyGenerator,
                       Logger logger) {
        this.providers = providers;
        this.rulesProvider = rulesProvider;
        this.bodyGenerator = bodyGenerator;
        this.logger = logger;
    }

    public void transform(Context context) {
        logger.init(context);

        ClassPool pool = new ClassPool(true);
        Source source = new Source(context);
        ClassLister reader = providers.get(source.getType()).getReader(source.getRoot());
        Writer writer = providers.get(source.getOutputType()).getWriter(context.getOutputRoot());
        List<String> classList = reader.list();

        Matchers rules = getMatchers(context);
        try {
            pool.appendClassPath(source.getRoot());
            logger.log(Noisy, "Using %s as source", context.getSourceRoot());
            if (context.hasClassPath()) {
                logger.log(Noisy, "Adding %s as additional classpath", context.getClassPath());
                pool.appendPathList(context.getClassPath());
            }
        } catch (NotFoundException e) {
            throw new TransformationException("Failed to load source classes from " + source.getRoot(), e);
        }

        logger.log(Default, "Classes to process: %d", classList.size());
        for (String className : classList) {
            try {
                transformClass(context, className, pool, rules, writer);
            } catch (NotFoundException | IOException e) {
                throw new TransformationException("Failed to load class " + className, e);
            }
        }

        try {
            injectJavassistRuntime(pool, writer);
        } catch (NotFoundException | IOException ignored) {
            logger.log(Default, "Failed to inject required javassist runtime class, results may be not usable");
        }

        writer.close();

        logger.log(Default, "Done");
    }

    private static boolean isModifier(CtClass cls, int modifier) {
        return (cls.getModifiers() & modifier) == modifier;
    }

    private static boolean isModifier(CtMember method, int modifier) {
        return (method.getModifiers() & modifier) == modifier;
    }

    private static void storeClass(Writer writer, CtClass cls) throws IOException {
        try {
            writer.addClass(cls.getName(), cls.toBytecode());
        } catch (CannotCompileException e) {
            throw new TransformationException("Failed to create bytecode for " + cls.getName(), e);
        }
    }

    private static void replaceMethodBody(CtBehavior method, String methodBody) {
        try {
            method.setBody(methodBody);
        } catch (CannotCompileException e) {
            throw new TransformationException(String.format("Cannot compile body for method %s. Source:\n%s", method.getLongName(), methodBody), e);
        }
    }

    private void injectJavassistRuntime(ClassPool pool, Writer writer) throws NotFoundException, IOException {
        logger.log(Verbose, "Injecting javassist runtime");
        CtClass javassistDesc = pool.get(Desc.class.getCanonicalName());
        storeClass(writer, javassistDesc);
    }

    private void transformClass(Context context, String className, ClassPool pool, Matchers rules, Writer writer) throws NotFoundException, IOException {
        CtClass cls = pool.get(className);
        if (rules.shouldSkipClass(className)) {
            logger.log(Verbose, "Skipping class %s", className);
            storeClass(writer, cls);
            return;
        }
        if (rules.shouldStripClass(className)) {
            logger.log(Verbose, "Stripping class %s", className);
            return;
        }
        if (context.shouldIgnoreNonPublic() && !isModifier(cls, PUBLIC)) {
            logger.log(Verbose, "Ignoring non-public class %s", className);
            return;
        }
        if (context.shouldStripFinals() && isModifier(cls, FINAL)) {
            logger.log(Verbose, "Stripping final modifier from class %s", className);
            cls.setModifiers(cls.getModifiers() - FINAL);
        }
        transformConstructors(context, rules, cls);
        transformMethods(context, rules, cls);
        transformFields(context, cls);
        storeClass(writer, cls);
    }

    private void transformFields(Context context, CtClass cls) throws NotFoundException {
        for (CtField field : cls.getDeclaredFields()) {
            if (context.shouldStripFields()) {
                logger.log(Noisy, "Removing field %s from class %s", field.getName(), cls.getName());
                cls.removeField(field);
            } else if (context.shouldIgnoreNonPublic() && !isModifier(field, PUBLIC)) {
                logger.log(Noisy, "Removing non-public field %s from class %s", field.getName(), cls.getName());
                cls.removeField(field);
            }
        }
    }

    private void transformConstructors(Context context, Matchers rules, CtClass cls) throws NotFoundException {
        for (CtConstructor constructor : cls.getConstructors()) {
            if (context.shouldIgnoreNonPublic() && !isModifier(constructor, PUBLIC)) {
                logger.log(Noisy, "Removing constructor %s from class %s", constructor.getName(), cls.getName());
                cls.removeConstructor(constructor);
            } else {
                logger.log(Noisy, "Removing class %s constructor %s body", cls.getName(), constructor.getSignature());
                replaceMethodBody(constructor, null);
            }
        }
    }

    private void transformMethods(Context context, Matchers rules, CtClass cls) throws NotFoundException {
        for (CtMethod method : cls.getDeclaredMethods()) {
            if (context.shouldStripFinals() && isModifier(method, FINAL)) {
                logger.log(Noisy, "Removing final modifier from method %s in class %s", method.getName(), cls.getName());
                method.setModifiers(method.getModifiers() - FINAL);
            }
            if (context.shouldIgnoreNonPublic() && !isModifier(method, PUBLIC)) {
                logger.log(Noisy, "Removing method %s from class %s", method.getName(), cls.getName());
                cls.removeMethod(method);
            } else {
                Optional<MethodMatcher> matcher = rules.findMatcher(method, context.shouldIgnoreDuplicateMatchers());
                String methodBody = matcher.isPresent() ? matcher.get().getMethodBody() : null;
                methodBody = bodyGenerator.alterBody(context, cls, method, methodBody);
                replaceMethodBody(method, methodBody);
            }
        }
    }

    private Matchers getMatchers(Context context) {
        Matchers rules;
        try {
            rules = rulesProvider.getMatchers(context.getRulesStream());
            logger.log(Noisy, "Loaded rules from %s", context.getRulesFile() == null ? "defaults" : context.getRulesFile());
        } catch (FileNotFoundException e) {
            throw new TransformationException("Cannot find rules file", e);
        }
        return rules;
    }

}
