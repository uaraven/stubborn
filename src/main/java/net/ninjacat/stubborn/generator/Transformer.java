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
import net.ninjacat.stubborn.exceptions.TransformationException;
import net.ninjacat.stubborn.file.*;
import net.ninjacat.stubborn.generator.rules.Matchers;
import net.ninjacat.stubborn.generator.rules.MethodMatcher;
import net.ninjacat.stubborn.generator.rules.RulesProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javassist.Modifier.FINAL;
import static javassist.Modifier.PUBLIC;

public class Transformer {

    private final Map<ClassPathType, ClassAccessProvider> providers;
    private final RulesProvider rulesProvider;

    @Inject
    public Transformer(Map<ClassPathType, ClassAccessProvider> providers, RulesProvider rulesProvider) {
        this.providers = providers;
        this.rulesProvider = rulesProvider;
    }

    public void transform(Context context) {
        ClassPool pool = new ClassPool(true);
        Source source = new Source(context);
        try {
            ClassLister reader = providers.get(source.getType()).getReader(source.getRoot());
            Writer writer = providers.get(source.getOutputType()).getWriter(context.getOutputRoot());
            List<String> classList = reader.list();
            Matchers rules = rulesProvider.getMatchers(context.getRulesStream());
            pool.appendClassPath(source.getRoot());
            for (String className : classList) {
                transformClass(context, className, pool, rules, writer);
            }
            writer.close();
        } catch (Exception ex) {
            throw new TransformationException(ex);
        }
    }

    private void transformClass(Context context, String className, ClassPool pool, Matchers rules, Writer writer) throws NotFoundException, CannotCompileException, IOException {
        CtClass cls = pool.get(className);
        if (context.shouldIgnoreNonPublic() && !isModifier(cls, PUBLIC)) {
            return;
        }
        if (context.shouldStripFinals() && isModifier(cls, FINAL)) {
            cls.setModifiers(cls.getModifiers() - FINAL);
        }
        transformMethods(context, rules, cls);
        transformFields(context, cls);
        storeClass(writer, cls);
    }

    private void transformFields(Context context, CtClass cls) throws NotFoundException {
        for (CtField field : cls.getDeclaredFields()) {
            if (context.shouldStripFields()) {
                cls.removeField(field);
            } else {
                if (context.shouldStripFinals() && isModifier(field, FINAL)) {
                    field.setModifiers(field.getModifiers() - FINAL);
                }
                if (context.shouldIgnoreNonPublic() && !isModifier(field, PUBLIC)) {
                    cls.removeField(field);
                }
            }
        }
    }

    private void transformMethods(Context context, Matchers rules, CtClass cls) throws NotFoundException, CannotCompileException {
        for (CtMethod method : cls.getDeclaredMethods()) {
            if (context.shouldStripFinals() && isModifier(method, FINAL)) {
                method.setModifiers(method.getModifiers() - FINAL);
            }
            if (context.shouldIgnoreNonPublic() && !isModifier(method, PUBLIC)) {
                cls.removeMethod(method);
            } else {
                Optional<MethodMatcher> matcher = rules.findMatcher(method, context.shouldIgnoreDuplicateMatchers());
                String methodBody = matcher.isPresent() ? matcher.get().getMethodBody() : null;
                replaceMethodBody(method, methodBody);
            }
        }
    }

    private boolean isModifier(CtClass cls, int modifier) {
        return (cls.getModifiers() & modifier) == modifier;
    }

    private boolean isModifier(CtMember method, int modifier) {
        return (method.getModifiers() & modifier) == modifier;
    }

    private void storeClass(Writer writer, CtClass cls) throws IOException, CannotCompileException {
        writer.addClass(cls.getName(), cls.toBytecode());
    }

    private void replaceMethodBody(CtMethod method, String methodBody) throws CannotCompileException {
        method.setBody(methodBody);
    }

}
