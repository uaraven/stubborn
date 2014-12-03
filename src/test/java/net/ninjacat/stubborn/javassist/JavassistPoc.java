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

package net.ninjacat.stubborn.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import net.ninjacat.stubborn.fixtures.Test1;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Not a unit tests per se, but a sandbox for testing various possible replacements
 */
public class JavassistPoc {

    @Test
    public void shouldCreateNewObjectInstance() throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass cls = pool.get(Test1.class.getCanonicalName());
        CtMethod method = cls.getMethod("getPojo", "()Lnet/ninjacat/stubborn/test/Pojo;");
        method.setBody("return ($r)$type.newInstance();");
        cls.setName("TestPojo");

        Object instance = cls.toClass().getConstructor().newInstance();

        Method getPojo = instance.getClass().getMethod("getPojo");
        Object pojo = getPojo.invoke(instance);

        Assert.assertNotNull("Should return instance of POJO", pojo);
    }

    @Test
    public void shouldCorrectlyReplaceBooleanReturn() throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass cls = pool.get(Test1.class.getCanonicalName());
        CtMethod method = cls.getMethod("getBool", "()Ljava/lang/Boolean;");
        method.setBody("return new java.lang.Boolean(false);");
    }


    @Test
    public void shouldCorrectlyReplaceIntegerReturn() throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass cls = pool.get(Test1.class.getCanonicalName());
        CtMethod method = cls.getMethod("getInt", "()Ljava/lang/Integer;");
        method.setBody("return new java.lang.Integer(0);");
    }

    @Test
    public void shouldCorrectlyReplaceFloatReturn() throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass cls = pool.get(Test1.class.getCanonicalName());
        CtMethod method = cls.getMethod("getFloat", "()Ljava/lang/Float;");
        method.setBody("return new java.lang.Float(0f);");
    }

    @Test
    public void shouldCorrectlyReplaceShortReturn() throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass cls = pool.get(Test1.class.getCanonicalName());
        CtMethod method = cls.getMethod("getShort", "()Ljava/lang/Short;");
        method.setBody("return new java.lang.Short((short)0);");
    }
}
