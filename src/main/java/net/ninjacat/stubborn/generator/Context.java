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

import javassist.bytecode.ClassFile;
import org.apache.commons.cli.CommandLine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Context {
    public static final String SOURCE = "source";
    public static final String OUTPUT = "output";
    public static final String TRANSFORM_RULES = "transform-rules";
    public static final String CLASSPATH = "classpath";
    public static final String STRIP_NON_PUBLIC = "strip-non-public";
    public static final String STRIP_FINAL = "strip-final";
    public static final String STRIP_FIELDS = "strip-fields";
    public static final String IGNORE_DUPLICATE_MATCHERS = "ignore-duplicate-matchers";
    public static final String GENERATE_INSTANCES = "generate-instances";
    public static final String TARGET_VERSION = "target";
    public static final String VERBOSE = "verbose";

    private static final Map<Integer, Integer> TARGET_VERSION_MAP = new HashMap<>();

    static {
        TARGET_VERSION_MAP.put(1, ClassFile.JAVA_1);
        TARGET_VERSION_MAP.put(2, ClassFile.JAVA_2);
        TARGET_VERSION_MAP.put(3, ClassFile.JAVA_3);
        TARGET_VERSION_MAP.put(4, ClassFile.JAVA_4);
        TARGET_VERSION_MAP.put(5, ClassFile.JAVA_5);
        TARGET_VERSION_MAP.put(6, ClassFile.JAVA_6);
        TARGET_VERSION_MAP.put(7, ClassFile.JAVA_7);
        TARGET_VERSION_MAP.put(8, ClassFile.JAVA_8);
    }

    private static final String DEFAULT_RULES_FILE = "/default-rules.xml";
    private final boolean stripNonPublic;
    private final boolean stripFinals;
    private final boolean stripFields;
    private final ReturnObjects objectReturnStrategy;
    private final String sourceRoot;
    private final String outputRoot;
    private final String rules;
    private final String classPath;
    private final boolean ignoreDuplicateMatchers;
    private final int logLevel;
    private final int target;

    public Context(CommandLine commandLine) {
        sourceRoot = commandLine.getOptionValue(SOURCE);
        outputRoot = commandLine.getOptionValue(OUTPUT);
        rules = commandLine.getOptionValue(TRANSFORM_RULES);
        classPath = commandLine.hasOption(CLASSPATH) ? commandLine.getOptionValue(CLASSPATH) : "";
        stripNonPublic = commandLine.hasOption(STRIP_NON_PUBLIC);
        stripFinals = commandLine.hasOption(STRIP_FINAL);
        stripFields = commandLine.hasOption(STRIP_FIELDS);
        ignoreDuplicateMatchers = commandLine.hasOption(IGNORE_DUPLICATE_MATCHERS);
        objectReturnStrategy = commandLine.hasOption(GENERATE_INSTANCES) ? ReturnObjects.Instance : ReturnObjects.Nulls;

        int targetVersion = commandLine.hasOption(TARGET_VERSION) ? tryParseInt(commandLine.getOptionValue(TARGET_VERSION), 0) : 0;
        if (TARGET_VERSION_MAP.containsKey(targetVersion)) {
            target = TARGET_VERSION_MAP.get(targetVersion);
        } else {
            target = 0;
        }

        int loggingLevel = commandLine.hasOption(VERBOSE) ? 1 : 0;
        loggingLevel = tryParseInt(commandLine.getOptionValue(VERBOSE), loggingLevel);
        logLevel = loggingLevel;
    }

    public String getSourceRoot() {
        return sourceRoot;
    }

    public String getOutputRoot() {
        return outputRoot;
    }

    public String getRulesFile() {
        return rules;
    }

    public boolean hasClassPath() {
        return classPath != null && !classPath.isEmpty();
    }

    public String getClassPath() {
        return classPath;
    }

    public InputStream getRulesStream() throws FileNotFoundException {
        return rules == null || rules.isEmpty() ? getClass().getResourceAsStream(DEFAULT_RULES_FILE) : new FileInputStream(rules);
    }

    public boolean shouldStripFinals() {
        return stripFinals;
    }

    public boolean shouldIgnoreNonPublic() {
        return stripNonPublic;
    }

    public boolean shouldStripFields() {
        return stripFields;
    }

    public boolean shouldIgnoreDuplicateMatchers() {
        return ignoreDuplicateMatchers;
    }

    public ReturnObjects getObjectReturnStrategy() {
        return objectReturnStrategy;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public int getTargetVersion() {
        return target;
    }

    private int tryParseInt(String optionValue, int defaultVersion) {
        try {
            return Integer.parseInt(optionValue);
        } catch (NumberFormatException ignored) {
            return defaultVersion;
        }
    }
}
