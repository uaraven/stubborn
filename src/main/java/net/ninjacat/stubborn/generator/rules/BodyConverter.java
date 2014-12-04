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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.Optional;

/**
 * User: ovoronin
 * Date: 12/4/2014
 */
public class BodyConverter implements Converter {
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        String keep = reader.getAttribute("keep");
        if ("yes".equalsIgnoreCase(keep) || "true".equalsIgnoreCase(keep)) {
            return Optional.empty();
        } else {
            String body = reader.getValue();
            return Optional.of(body);
        }
    }

    @Override
    public boolean canConvert(Class aClass) {
        return aClass.equals(Optional.class);
    }
}