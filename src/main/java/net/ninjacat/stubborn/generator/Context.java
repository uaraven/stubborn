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

import org.apache.commons.cli.CommandLine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Context {
    private static final String DEFAULT_RULES_FILE = "/default-rules.xml";

    private final boolean stripNonPublic;
    private final boolean stripFinals;
    private final boolean stripFields;
    private final ReturnObjects objectReturnStrategy;
    private final String sourceRoot;
    private final String outputRoot;
    private final String rules;
    private final boolean ignoreDuplicateMatchers;
    private final int logLevel;

    public Context(CommandLine commandLine) {
        sourceRoot = commandLine.getOptionValue("source");
        outputRoot = commandLine.getOptionValue("output");
        rules = commandLine.getOptionValue("transform-rules");
        stripNonPublic = commandLine.hasOption("strip-non-public");
        stripFinals = commandLine.hasOption("strip-final");
        stripFields = commandLine.hasOption("strip-fields");
        ignoreDuplicateMatchers = commandLine.hasOption("ignore-duplicate-matchers");
        objectReturnStrategy = commandLine.hasOption("return-instances") ? ReturnObjects.Instance : ReturnObjects.Nulls;
        int loggingLevel = commandLine.hasOption("v") ? 1 : 0;
        try {
            loggingLevel = Integer.valueOf(commandLine.getOptionValue("verbose"));
        } catch (Exception ignored) {
        }
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
}
