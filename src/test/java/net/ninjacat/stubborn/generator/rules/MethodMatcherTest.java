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

import javassist.CtMethod;
import net.ninjacat.stubborn.fixtures.Test1;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MethodMatcherTest {

    @Test
    public void shouldMatchMethodByReturnTypeOnly() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "getInt");

        MethodMatcher matcher = new MethodMatcher("java.lang.Integer", null, null, null, null);

        assertTrue("Should match method by return type", matcher.isMatching(method));
    }

    @Test
    public void shouldMatchMethodBySignatureOnly() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "add");

        MethodMatcher matcher = new MethodMatcher(null, null, null, "(II)J", null);

        assertTrue("Should match method by signature", matcher.isMatching(method));
    }

    @Test
    public void shouldMatchMethodByNameOnly() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "interestingMethodName");

        MethodMatcher matcher = new MethodMatcher(null, null, "inte[rp]est.*[^K]ame", null, null);

        assertTrue("Should match method by name", matcher.isMatching(method));
    }

    @Test
    public void shouldMatchMethodByNameAndClassName() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "interestingMethodName");

        MethodMatcher matcher = new MethodMatcher(null, ".*Test\\d", "inte[rp]est.*[^K]ame", null, null);

        assertTrue("Should match method by name and class name", matcher.isMatching(method));
    }

    @Test
    public void shouldMatchMethodByNameAndReturnType() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "interestingMethodName");

        MethodMatcher matcher = new MethodMatcher("void", null, "inte[rp]est.*[^K]ame", null, null);

        assertTrue("Should match method by name and return type", matcher.isMatching(method));
    }

    @Test
    public void shouldMatchMethodBySignatureAndNameAndReturnType() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "interestingMethodName");

        MethodMatcher matcher = new MethodMatcher("void", null, "inte[rp]est.*[^K]ame", "()V", null);

        assertTrue("Should match method by signature, name and return type", matcher.isMatching(method));
    }

    @Test
    public void shouldNotMatchMethodByNameAndWrongClassName() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "interestingMethodName");

        MethodMatcher matcher = new MethodMatcher(null, "Test6", "inte[rp]est.*[^K]ame", null, null);

        assertFalse("Should not match method by name and wrong class name", matcher.isMatching(method));
    }

    @Test
    public void shouldNotMatchMethodWithDifferentSignature() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "sub");

        MethodMatcher matcher = new MethodMatcher(null, null, null, "(II)J", null);

        assertFalse("Should not match method with different signature", matcher.isMatching(method));
    }

    @Test
    public void shouldNotMatchMethodWithDifferentReturnType() throws Exception {
        CtMethod method = ClassFixtures.getMethod(Test1.class, "sub");

        MethodMatcher matcher = new MethodMatcher("boolean", null, null, null, null);

        assertFalse("Should not match method with different return type", matcher.isMatching(method));
    }
}
