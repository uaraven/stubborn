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

package net.ninjacat.stubborn.generator.rules;

import javassist.CtMethod;
import net.ninjacat.stubborn.exceptions.TransformationException;
import net.ninjacat.stubborn.fixtures.Test1;
import org.junit.Test;

import java.util.Optional;

import static net.ninjacat.stubborn.generator.rules.ClassFixtures.getMethod;
import static org.junit.Assert.*;

public class MatchersTest {

    @Test
    public void shouldFindStringGetter() throws Exception {
        Matchers matchers = Matchers.loadFromStream(getClass().getResourceAsStream("/string-getter.xml"));

        CtMethod getString = getMethod(Test1.class, "getString");

        Optional<MethodMatcher> matcher = matchers.findMatcher(getString, false);

        assertTrue("Should find exactly one matcher", matcher.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWithDuplicateGetters() throws Exception {
        Matchers matchers = Matchers.loadFromStream(getClass().getResourceAsStream("/duplicate-getter.xml"));

        CtMethod getString = getMethod(Test1.class, "getString");

        matchers.findMatcher(getString, false);
    }


    @Test
    public void shouldSelectFirstMatcherFromDuplicate() throws Exception {
        Matchers matchers = Matchers.loadFromStream(getClass().getResourceAsStream("/duplicate-getter.xml"));

        CtMethod getString = getMethod(Test1.class, "getString");

        Optional<MethodMatcher> matcher = matchers.findMatcher(getString, true);

        assertTrue("Should find exactly one matcher", matcher.isPresent());
        assertEquals("Should select first matcher", matcher.get().getMethodBody(), "return \"get-string\";");
    }

    @Test(expected = TransformationException.class)
    public void shouldFailWhenLoading() throws Exception {
        Matchers.loadFromStream(getClass().getResourceAsStream("/no-conditions-matcher.xml"));
    }

    @Test
    public void shouldLoadClassesToStrip() throws Exception {
        Matchers matchers = Matchers.loadFromStream(getClass().getResourceAsStream("/strip-class-getter.xml"));

        assertTrue("Should load list of classes to strip", matchers.shouldStripClass("java.util.Date"));
        assertTrue("Should load list of classes to strip", matchers.shouldStripClass("java.lang.CharSequence"));
    }

    @Test
    public void shouldNotAllowToStripClassesNotInRules() throws Exception {
        Matchers matchers = Matchers.loadFromStream(getClass().getResourceAsStream("/strip-class-getter.xml"));

        assertFalse("Should not allow to strip unlisted class", matchers.shouldStripClass("java.sql.Date"));
    }

    @Test
    public void shouldNotFailIfRulesDoesNotContainClasses() throws Exception {
        Matchers matchers = Matchers.loadFromStream(getClass().getResourceAsStream("/string-getter.xml"));

        assertFalse("Should not allow to strip unlisted class", matchers.shouldStripClass("java.sql.Date"));
    }

}
