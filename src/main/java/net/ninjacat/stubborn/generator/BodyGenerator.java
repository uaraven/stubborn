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

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.ninjacat.stubborn.reflect.Types;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import static net.ninjacat.stubborn.generator.LogLevel.Verbose;

public class BodyGenerator {

    private final Logger logger;

    @Inject
    public BodyGenerator(Logger logger) {
        this.logger = logger;
    }

    private static String injectMethodVariable(String methodBody, CtMethod method) {
        String result = methodBody.replaceAll("\\$method", "\"" + method.getName() + "\"");
        result = result.replaceAll("\\$sign", "\"" + method.getSignature() + "\"");
        return result;
    }

    public String alterBody(Context context, CtClass cls, CtMethod method, String methodBody) {
        if (methodBody == null) {
            logger.log(Verbose, "Rewriting method %s in class %s with default body", method.getName(), cls.getName());
            if (context.getObjectReturnStrategy() == ReturnObjects.Instance) {
                methodBody = generateReturnInstance(method);
            }
        } else {
            methodBody = injectMethodVariable(methodBody, method);
            logger.log(Verbose, "Rewriting method %s in class %s", method.getName(), cls.getName());
        }
        return methodBody;
    }

    @Nullable
    private String generateReturnInstance(CtMethod method) {
        try {
            CtClass returnType = method.getReturnType();
            if (Types.isBoxType(returnType)) {
                return "return " + Types.getDefaultValueLiteral(returnType) + ";";
            } else if (requiresCustomReturn(returnType)) {
                return "return ($r)$type.newInstance();";
            }
        } catch (NotFoundException ignored) {
        }
        return null;
    }

    private boolean requiresCustomReturn(CtClass returnType) {
        return !returnType.isPrimitive() && !returnType.isArray() && !returnType.isEnum();
    }


}