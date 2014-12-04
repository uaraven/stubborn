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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.Optional;
import java.util.regex.Pattern;

@XStreamAlias("method")
public class MethodMatcher {
    @XStreamAlias("returntype")
    private final String returnType;
    @XStreamAlias("classname")
    private final String className;
    @XStreamAlias("methodname")
    private final String methodName;
    @XStreamAlias("signature")
    private final String signature;
    @XStreamAlias("body")
    @XStreamConverter(BodyConverter.class)
    private final Optional<String> methodBody;

    private Pattern classNameRe;
    private Pattern methodNameRe;

    public MethodMatcher(String returnType, String className, String methodName, String signature, String methodBody) {
        this.returnType = returnType;
        this.className = className;
        this.methodName = methodName;
        this.signature = signature;
        this.methodBody = Optional.ofNullable(methodBody);
    }

    public boolean isMissingConditions() {
        return isEmpty(returnType) && isEmpty(className) && isEmpty(methodName) && isEmpty(signature);
    }

    public boolean isMatching(CtMethod method) {
        return isMatchingSignature(method) && isMatchingParent(method) && isMatchingName(method) && isMatchingResult(method);
    }

    public boolean shouldKeepBody() {
        return !methodBody.isPresent();
    }

    public String getMethodBody() {
        return methodBody.orElse(null);
    }

    public String repr() {
        return "MethodMatcher{" +
                "returnType='" + returnType + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", signature='" + signature + '\'' +
                "}\n";
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private boolean isMatchingResult(CtMethod method) {
        try {
            return isEmpty(returnType) || method.getReturnType().getName().equals(returnType);
        } catch (NotFoundException ignored) {
            return false;
        }
    }

    private boolean isMatchingSignature(CtMember method) {
        return isEmpty(signature) || signature.equals(method.getSignature());
    }

    private boolean isMatchingParent(CtMember method) {
        if (isEmpty(className)) {
            return true;
        }
        Pattern namePattern = getClassNamePattern();
        return namePattern.matcher(method.getDeclaringClass().getName()).matches();
    }

    private boolean isMatchingName(CtMember method) {
        if (isEmpty(methodName)) {
            return true;
        }
        Pattern namePattern = getMethodNamePattern();
        return namePattern.matcher(method.getName()).matches();
    }

    private Pattern getClassNamePattern() {
        if (classNameRe == null) {
            classNameRe = Pattern.compile(className);
        }
        return classNameRe;
    }

    private Pattern getMethodNamePattern() {
        if (methodNameRe == null) {
            methodNameRe = Pattern.compile(methodName);
        }
        return methodNameRe;
    }
}
