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

import javassist.*;
import javassist.runtime.Desc;
import net.ninjacat.stubborn.exceptions.TransformationException;
import net.ninjacat.stubborn.file.ClassAccessProvider;
import net.ninjacat.stubborn.file.ClassPathType;
import net.ninjacat.stubborn.file.Source;
import net.ninjacat.stubborn.file.Writer;
import net.ninjacat.stubborn.generator.rules.MethodMatcher;
import net.ninjacat.stubborn.generator.rules.RulesProvider;
import net.ninjacat.stubborn.generator.rules.TransformRules;
import net.ninjacat.stubborn.log.Logger;
import net.ninjacat.stubborn.transform.Context;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static javassist.Modifier.*;
import static net.ninjacat.stubborn.generator.ClassUtils.appendClasses;
import static net.ninjacat.stubborn.generator.ClassUtils.getInputClassList;
import static net.ninjacat.stubborn.log.LogLevel.*;

public class Transformer {

    private final Map<ClassPathType, ClassAccessProvider> providers;
    private final RulesProvider rulesProvider;
    private final BodyGenerator bodyGenerator;
    private final Logger logger;
    private final ClassInjector injector;

    @Inject
    public Transformer(Map<ClassPathType, ClassAccessProvider> providers,
                       RulesProvider rulesProvider,
                       BodyGenerator bodyGenerator,
                       ClassInjector injector,
                       Logger logger) {
        this.injector = injector;
        this.providers = Collections.unmodifiableMap(providers);
        this.rulesProvider = rulesProvider;
        this.bodyGenerator = bodyGenerator;
        this.logger = logger;
    }

    public void transform(Context context) {
        logger.init(context);

        ClassPool pool = new ClassPool(false);

        List<Source> sources = context.getSources();
        List<String> classList = getInputClassList(providers, sources);
        Writer writer = providers.get(context.getOutputType()).getWriter(context.getOutputRoot());

        addSourceClassPaths(pool, sources);
        addExtraClassPath(context, pool);
        pool.appendSystemPath();

        logger.log(Default, "Classes to process: %d", classList.size());

        if (context.getTargetVersion() > 0) {
            logger.log(Verbose, "Using %s as class file version", context.getTargetVersion());
        }

        TransformRules rules = getMatchers(context);
        for (String className : classList) {
            try {
                transformClass(context, className, pool, rules, writer);
            } catch (NotFoundException | IOException e) {
                throw new TransformationException("Failed to load class " + className, e);
            }
        }

        if (rules.hasInjectRules()) {
            logger.log(Verbose, "Injecting classes");
            injector.injectClasses(writer, rules.getInjectRules());
        }
        injectJavassistRuntime(context, pool, writer);

        writer.close();

        logger.log(Default, "Done");
    }

    private static boolean isNonModifiableMethod(CtMember method) {
        int modifiers = method.getModifiers();
        return isAbstract(modifiers) || isNative(modifiers);
    }

    private static boolean isModifier(CtClass cls, int modifier) {
        return (cls.getModifiers() & modifier) == modifier;
    }

    private static boolean isModifier(CtMember method, int modifier) {
        return (method.getModifiers() & modifier) == modifier;
    }

    private static void storeClass(Context context, Writer writer, CtClass cls) throws IOException {
        try {
            if (context.getTargetVersion() > 0) {
                cls.getClassFile().setMajorVersion(context.getTargetVersion());
            }
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


    private void addSourceClassPaths(ClassPool pool, Collection<Source> sources) {
        sources.stream().forEach(s -> {
            appendClasses(pool, s.getRoot());
            logger.log(Noisy, "Using %s as source", s.getRoot());
        });
    }

    private void addExtraClassPath(Context context, ClassPool pool) {
        if (context.hasClassPath()) {
            logger.log(Noisy, "Adding %s as additional classpath", context.getClassPath());
            try {
                pool.appendPathList(context.getClassPath());
            } catch (NotFoundException e) {
                throw new TransformationException("Failed to load source classes from " + context.getClassPath(), e);
            }
        }
    }

    private void injectJavassistRuntime(Context context, ClassPool pool, Writer writer) {
        logger.log(Verbose, "Injecting javassist runtime");
        try {
            CtClass javassistDesc = pool.get(Desc.class.getCanonicalName());
            storeClass(context, writer, javassistDesc);
        } catch (NotFoundException | IOException ignored) {
            logger.err("Failed to inject required javassist runtime class, results may be not usable");
        }
    }

    private void transformClass(Context context, String className, ClassPool pool, TransformRules rules, Writer writer) throws NotFoundException, IOException {
        CtClass cls = pool.get(className);
        if (cls.isFrozen()) {
            logger.log(Verbose, "Stripping frozen class %s", className);
            return;
        }
        if (rules.shouldSkipClass(className)) {
            writeUnchanged(context, "class", writer, cls);
            return;
        }
        if (rules.shouldStripClass(className)) {
            logger.log(Verbose, "Stripping class %s", className);
            return;
        }
        if (cls.isInterface()) {
            writeUnchanged(context, "interface", writer, cls);
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
        transformConstructors(context, cls);
        transformMethods(context, rules, cls);
        transformFields(context, cls);
        storeClass(context, writer, cls);
    }

    private void writeUnchanged(Context context, String item, Writer writer, CtClass cls) throws IOException {
        logger.log(Verbose, "Skipping %s %s", item, cls.getName());
        storeClass(context, writer, cls);
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

    private void transformConstructors(Context context, CtClass cls) throws NotFoundException {
        for (CtConstructor constructor : cls.getDeclaredConstructors()) {
            if (context.shouldIgnoreNonPublic() && !isModifier(constructor, PUBLIC)) {
                logger.log(Noisy, "Deleting constructor %s from class %s", constructor.getName(), cls.getName());
                cls.removeConstructor(constructor);
            } else {
                logger.log(Noisy, "Removing constructor %s body from %s ", constructor.getSignature(), cls.getName());
                try {
                    if (isPrivate(constructor.getModifiers())) {
                        constructor.setModifiers(constructor.getModifiers() - PRIVATE);
                    }
                    replaceMethodBody(constructor, null);
                } catch (Exception ignored) {
                    logger.log(Default, "Failed to replace body of %s constructor in %s", constructor.getSignature(), cls.getName());
                }
            }
        }
    }

    private void transformMethods(Context context, TransformRules rules, CtClass cls) throws NotFoundException {
        for (CtMethod method : cls.getDeclaredMethods()) {
            if (context.shouldStripFinals() && isModifier(method, FINAL) && !isNative(method.getModifiers())) {
                logger.log(Noisy, "Removing final modifier from method %s in class %s", method.getName(), cls.getName());
                method.setModifiers(method.getModifiers() - FINAL);
            }
            if (context.shouldIgnoreNonPublic() && !isModifier(method, PUBLIC)) {
                logger.log(Noisy, "Removing method %s from class %s", method.getName(), cls.getName());
                cls.removeMethod(method);
            }
            if (isNonModifiableMethod(method)) {
                logger.log(Noisy, "Skipping unmodifiable method %s in class %s", method.getName(), cls.getName());
            } else {
                Optional<MethodMatcher> matcher = rules.findMatcher(method, context.shouldIgnoreDuplicateMatchers());
                String methodBody = matcher.isPresent() ? matcher.get().getMethodBody() : null;
                methodBody = bodyGenerator.alterBody(context, cls, method, methodBody);
                replaceMethodBody(method, methodBody);
            }
        }
    }

    private TransformRules getMatchers(Context context) {
        TransformRules rules;
        try {
            rules = rulesProvider.getMatchers(context.getRulesStream());
            logger.log(Noisy, "Loaded rules from %s", context.getRulesFile() == null ? "defaults" : context.getRulesFile());
        } catch (FileNotFoundException e) {
            throw new TransformationException("Cannot find rules file", e);
        }
        return rules;
    }

}
