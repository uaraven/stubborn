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
import net.ninjacat.stubborn.exceptions.TransformationException;
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
        } catch (MissingOptionException ex) {
            System.out.println("Missing required parameter " + ex.getMissingOptions());
            printHelp(options);
        } catch (TransformationException ex) {
            System.out.println("Failed to perform transformation caused by " + ex.getCause());
            System.out.println(ex.getMessage());
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("stubborn [OPTIONS] -s <source path> -o <output path>", options);
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static Options createOptions() {
        Option source = OptionBuilder.withArgName("path|jar").withLongOpt(Context.SOURCE).hasArg().isRequired().
                withDescription("Source path. Might be folder on a file system or a jar-file").create('s');
        Option output = OptionBuilder.withArgName("path|jar").withLongOpt(Context.OUTPUT).hasArg().isRequired().
                withDescription("Folder for output. Transformed classes will be stored to this folder").create('o');
        Option rules = OptionBuilder.withArgName("xml-file").withLongOpt(Context.TRANSFORM_RULES).hasArg().
                withDescription("Transformation rules file. If not specified, then default-rules.xml in current directory will be used").
                create('r');
        Option stripNonPublic = OptionBuilder.withLongOpt(Context.STRIP_NON_PUBLIC).
                withDescription("Only stub public methods, non-public methods will be stripped from output classes").
                create('n');
        Option stripFields = OptionBuilder.withLongOpt(Context.STRIP_FIELDS).
                withDescription("Remove field definitions from classes").
                create('m');
        Option stripFinals = OptionBuilder.withLongOpt(Context.STRIP_FINAL).
                withDescription("Remove final modifier from methods and classes").
                create('f');
        Option generateInstances = OptionBuilder.withLongOpt(Context.GENERATE_INSTANCES).
                withDescription("Generate return newInstanse() for reference return types").
                create("g");
        Option verbose = OptionBuilder.withArgName("level").withLongOpt(Context.VERBOSE).
                withDescription("Provide more output (-v 2 for even more output)").hasOptionalArg().withType(Integer.class).
                create('v');
        Option ignoreDupMatchers = OptionBuilder.withLongOpt(Context.IGNORE_DUPLICATE_MATCHERS).
                withDescription("Ignore duplicate matchers, use first defined.").create('i');
        Option classPath = OptionBuilder.withLongOpt(Context.CLASSPATH).hasArg().
                withDescription("Additional classpath to be used during transformation").create("c");
        Option help = new Option("h", "help", false, "Show this help message");

        Options options = new Options();
        options.addOption(source);
        options.addOption(output);
        options.addOption(rules);
        options.addOption(stripNonPublic);
        options.addOption(stripFields);
        options.addOption(stripFinals);
        options.addOption(generateInstances);
        options.addOption(verbose);
        options.addOption(ignoreDupMatchers);
        options.addOption(classPath);
        options.addOption(help);

        return options;
    }
}
