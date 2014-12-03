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

import javassist.CtClass;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.ninjacat.stubborn.log.Logger;
import net.ninjacat.stubborn.reflect.Types;
import net.ninjacat.stubborn.transform.Context;
import net.ninjacat.stubborn.transform.ReturnObjects;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import static net.ninjacat.stubborn.log.LogLevel.Noisy;
import static net.ninjacat.stubborn.log.LogLevel.Verbose;

public class BodyGenerator {

    private static final String SIGNATURE_PLACEHOLDER = "\\$sign";
    private static final String METHOD_NAME_PLACEHOLDER = "\\$method";
    private final Logger logger;

    @Inject
    public BodyGenerator(Logger logger) {
        this.logger = logger;
    }

    public String alterBody(Context context, CtClass cls, CtMethod method, String methodBody) {
        String body = methodBody;
        if (body == null) {
            if (context.getObjectReturnStrategy() == ReturnObjects.Instance) {
                logger.log(Verbose, "Rewriting method %s in class %s with default body",
                        method.getName(), method.getDeclaringClass().getName());
                body = generateReturnInstance(method);
            }
        } else {
            body = injectMethodVariable(methodBody, method);
            logger.log(Verbose, "Rewriting method %s in class %s", method.getName(), cls.getName());
        }
        return body;
    }

    private static String injectMethodVariable(String methodBody, CtMember method) {
        String result = methodBody.replaceAll(METHOD_NAME_PLACEHOLDER, "\"" + method.getName() + "\"");
        result = result.replaceAll(SIGNATURE_PLACEHOLDER, "\"" + method.getSignature() + "\"");
        return result;
    }

    private static boolean requiresCustomReturn(CtClass returnType) {
        return !returnType.isPrimitive() && !returnType.isArray() && !returnType.isEnum();
    }

    @Nullable
    private String generateReturnInstance(CtMethod method) {
        try {
            CtClass returnType = method.getReturnType();
            if (Types.isBoxType(returnType)) {
                logger.log(Noisy, "Using boxed wrapper constructor in method %s in class %s",
                        method.getName(), method.getDeclaringClass().getName());
                return "return " + Types.getDefaultValueLiteral(returnType) + ";";
            } else if (requiresCustomReturn(returnType)) {
                logger.log(Noisy, "Using newInstance() in method %s in class %s",
                        method.getName(), method.getDeclaringClass().getName());
                return "return ($r)$type.newInstance();";
            }
        } catch (NotFoundException ignored) {
        }
        return null;
    }


}
