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

package net.ninjacat.stubborn;

import net.ninjacat.stubborn.config.Bootstrapper;
import net.ninjacat.stubborn.generator.Context;
import net.ninjacat.stubborn.generator.Transformer;
import org.apache.commons.cli.*;

public class Stubborn {

    private Stubborn() {
    }

    public static void main(String[] argv) throws ClassNotFoundException, ParseException {
        Options options = createOptions();
        if (argv.length == 0) {
            printHelp(options);
            return;
        }

        Class.forName(Bootstrapper.class.getCanonicalName());
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(options, argv);
            if (commandLine.hasOption("h")) {
                printHelp(options);
                return;
            }

            Context context = new Context(commandLine);

            Transformer transformer = Bootstrapper.get(Transformer.class);

            transformer.transform(context);
        } catch (MissingOptionException ignored) {
            System.out.println("Missing required parameter");
            printHelp(options);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("stubborn [OPTIONS] -s <source path> -o <output path>", options);
    }

    private static Options createOptions() {
        Option source = OptionBuilder.withArgName("path|jar").withLongOpt("source").hasArg().isRequired().
                withDescription("Source path. Might be folder on a file system or a jar-file").create('s');
        Option output = OptionBuilder.withArgName("path|jar").withLongOpt("output").hasArg().isRequired().
                withDescription("Folder for output. Transformed classes will be stored to this folder").create('o');
        Option rules = OptionBuilder.withArgName("xml-file").withLongOpt("transform-rules").hasArg().
                withDescription("Transformation rules file. If not specified, then default-rules.xml in current directory will be used").
                create('r');
        Option stripNonPublic = OptionBuilder.withLongOpt("strip-non-public").
                withDescription("Only stub public methods, non-public methods will be stripped from output classes").
                create('n');
        Option stripFields = OptionBuilder.withLongOpt("strip-fields").
                withDescription("Remove field definitions from classes").
                create('m');
        Option stripFinals = OptionBuilder.withLongOpt("strip-final").
                withDescription("Remove final modifier from methods and classes").
                create('f');
        Option verbose = OptionBuilder.withArgName("level").withLongOpt("verbose").
                withDescription("Provide more output").hasOptionalArg().withType(Integer.class).
                create('v');
        Option ignoreDupMatchers = OptionBuilder.withLongOpt("ignore-duplicate-matchers").
                withDescription("Ignore duplicate matchers, use first defined.").create('i');
        Option help = new Option("h", "help", false, "Show this help message");

        Options options = new Options();
        options.addOption(source);
        options.addOption(output);
        options.addOption(rules);
        options.addOption(stripNonPublic);
        options.addOption(stripFields);
        options.addOption(stripFinals);
        options.addOption(verbose);
        options.addOption(ignoreDupMatchers);
        options.addOption(help);

        return options;
    }
}
