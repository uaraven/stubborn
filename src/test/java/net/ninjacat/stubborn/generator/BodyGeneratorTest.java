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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.ninjacat.stubborn.fixtures.Test1;
import net.ninjacat.stubborn.log.Logger;
import net.ninjacat.stubborn.transform.Context;
import net.ninjacat.stubborn.transform.ReturnObjects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class BodyGeneratorTest {

    @Mock
    private Context context;
    @Mock
    private Logger logger;
    private BodyGenerator bodyGenerator;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        bodyGenerator = new BodyGenerator(logger);
    }

    @Test
    public void shouldNotGenerateBodyWhenNoInstanceOptionSet() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Nulls);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getString", "()Ljava/lang/String;");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should leave body generation to javassist", null, generated);
    }

    @Test
    public void shouldNotGenerateBodyForPrimitiveTypeWhenReturnNulls() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Nulls);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getLong", "()J");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should leave body generation to javassist", null, generated);
    }


    @Test
    public void shouldNotGenerateBodyForPrimitiveTypeWhenReturnInstances() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Instance);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getLong", "()J");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should leave body generation to javassist", null, generated);
    }

    @Test
    public void shouldGenerateReturnInstanceWhenInstanceOptionSet() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Instance);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getString", "()Ljava/lang/String;");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should generate body with newInstance()", "return ($r)$type.newInstance();", generated);
    }


    @Test
    public void shouldGenerateReturnWrapperWhenInstanceOptionSetAndBoxedReturnType() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Instance);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getInt", "()Ljava/lang/Integer;");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should generate body with newInstance()", "return new java.lang.Integer(0);", generated);
    }

    @Test
    public void shouldGenerateReturnWrapperWhenInstanceOptionSetAndBooleanBoxedReturnType() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Instance);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getBool", "()Ljava/lang/Boolean;");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should generate body with newInstance()", "return new java.lang.Boolean(false);", generated);
    }

    @Test
    public void shouldGenerateReturnWrapperWhenInstanceOptionSetAndShortBoxedReturnType() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Instance);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getShort", "()Ljava/lang/Short;");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should generate body with newInstance()", "return new java.lang.Short((short)0);", generated);
    }


    @Test
    public void shouldGenerateReturnWrapperWhenInstanceOptionSetAndShortFloatReturnType() throws Exception {
        when(context.getObjectReturnStrategy()).thenReturn(ReturnObjects.Instance);

        CtClass cls = getTestClass();
        CtMethod method = cls.getMethod("getFloat", "()Ljava/lang/Float;");

        String generated = bodyGenerator.alterBody(context, cls, method, null);

        assertEquals("Should generate body with newInstance()", "return new java.lang.Float(0.0f);", generated);
    }

    private CtClass getTestClass() throws NotFoundException {
        ClassPool pool = new ClassPool(true);
        return pool.get(Test1.class.getCanonicalName());
    }
}
