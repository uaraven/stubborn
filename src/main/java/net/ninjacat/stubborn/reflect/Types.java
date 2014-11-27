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

package net.ninjacat.stubborn.reflect;

import javassist.CtClass;

import java.util.HashMap;
import java.util.Map;

public final class Types {

    private static final Map<String, String> BOXED_TYPES = new HashMap<>();

    static {
        BOXED_TYPES.put(Character.class.getCanonicalName(), "'\\u00'");
        BOXED_TYPES.put(Integer.class.getCanonicalName(), "0");
        BOXED_TYPES.put(Byte.class.getCanonicalName(), "(byte)0");
        BOXED_TYPES.put(Short.class.getCanonicalName(), "(short)0");
        BOXED_TYPES.put(Long.class.getCanonicalName(), "0L");
        BOXED_TYPES.put(Float.class.getCanonicalName(), "0.0f");
        BOXED_TYPES.put(Double.class.getCanonicalName(), "0.0");
        BOXED_TYPES.put(Boolean.class.getCanonicalName(), "false");
    }

    private Types() {
    }

    public static boolean isBoxType(CtClass cls) {
        return BOXED_TYPES.keySet().contains(cls.getName());
    }

    public static String getDefaultValueLiteral(CtClass cls) {
        return "new " + cls.getName() + "(" + BOXED_TYPES.get(cls.getName()) + ")";
    }

}
