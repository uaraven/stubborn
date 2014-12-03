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

package net.ninjacat.stubborn.fixtures;

import net.ninjacat.stubborn.test.Pojo;

/**
 * User: ovoronin
 * Date: 10/10/2014
 */
public class Test1 {
    public String getString() {
        return "1";
    }

    public Integer getInt() {
        return 1;
    }

    public Float getFloat() {
        return 1f;
    }

    public Short getShort() {
        return 1;
    }

    public Boolean getBool() {
        return true;
    }

    public long getLong() {
        return 10l;
    }

    public long add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public void interestingMethodName() {
    }

    public Pojo getPojo() {
        return null;
    }

    ;
}
