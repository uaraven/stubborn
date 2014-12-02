# Stubborn #

### What is this about ###

#### TL;DR ####

Stubborn is a command-line utility that cripples perfect Java classes into shadows of their former selves with stubbed methods. Might be useful for testing.

#### Longer Version ####

You know that feeling when you're writing unit tests and it comes down to a part of code which is calling
third party library and and you have no way of working around it. And you call that method of that third party
class and it has tons of side-effects. Or crashes. Or, maybe, you want to mock it, but it is in final class and
you are using Mockito. And you say, "Damn, I want to have this class stripped of all its BS just for testing".

Well, you asked for it.

Stubborn allows you to transform java classes into their test-friendly twins. You can remove finals, you can
rewrite methods to do what you need. You can use stubbed classes for testing with greater flexibility. 


### Development ###

To start working on Stubborn you will need Java 8 and maven 3. Clone this repository and start hacking.

To start using Stubborn you have following options:
 
 1. Clone this repository and run mvn package. This will produce jar file with all dependencies.
 2. Use Jar file from bin/ folder
 3. No there are no other options right now
 
### Usage ###

Stubborn uses Javassist to transform classes and rewrite methods, so you may want to familiarize yourself with
Javassist [syntax](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial2.html#intro)


Stubborn accepts folders and jar files as input and can produce folders and jar files as output. Magic happens in between. 
Rules by which magic happens are defined in rules file. 

#### Rules file ####

Rules file is an XML-file which defines how exactly should methods be stubbed.

Simplest rule file could be written as following:
```xml
<?xml version="1.0"?>
<rules>
    <methods>
        <method>
            <returntype>java.lang.String</returntype>
            <body>return "";</body>
        </method>
    </methods>
</rules>
```

This rule will match all methods that return String and will replace their body with a simple `return "";` Full syntax of a method matching rule is:

```xml
<method>
    <returntype></returntype>
    <classname></classname>
    <methodname></methodname>
    <signature></signature>
    <body></body>
</method>
```

Supported matchers:

* ReturnType - matches by canonical name of return type. For Boolean you should use `java.lang.Boolean`, for primitive types use type name, like `boolean`
* ClassName - regular expression which matches class name in which method is defined. Be careful, it matches full class name, so simple `ClassName` will not work. Use `com.example.ClassName` or `.*ClassName`
* MethodName - another regular expression which matches method name itself
* Signature - matches method signature, for example `(IZ)Ljava/lang/String;` will match any method which accepts `int` and `boolean` as parameters and returns `String`

You can skip any matcher. All supplied matchers are combined with AND operation. Obviously it makes no sense in using both ReturnType and Signature. Rule must contain at least one matcher.

Put your Java code inside `body` tag. This code will be compiled and will be used instead original method body. All limitations of Javassist apply.
 
See [Javassist Tutorial](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial2.html#intro) and especially [alteration section](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial2.html#alter) to get understanding how javassist method altering works and which special identifiers are supported. In addition to javassist identifiers Stubborn supports additional identifiers:

* `$method` which will be replaced with a string containing name of the current method
* `$sign` which will be replaced with a current method's signature.

If method is matched by multiple rules, Stubborn will fail immediately and tell you which matchers are conflicting. You can override this behavior with `--ignore-duplicate-matchers` parameter, in this case first matcher will be selected.

If method is not matched by any rules its body will be replaced with `return null;` for all methods which return objects and with `return 0;` for all methods which return numbers.

If no matching rules are supplied the default one will be used. It is actually the one shown in the example above. 
This behavior can be changed with `--generate-instances` option. When this option is specified Stubborn will generate newInstance() call for  methods which return reference types. Such types must have public default constructor. If return type is a primitive wrapper, then correct constructor call will be generated, for example `new java.lang.Float(0.0f)` for floats or `new java.lang.Boolean(false)` for booleans.
 
All methods which return `java.lang.String` will return empty string, other methods will be stubbed with default value (i.e. zero, null or false).

You can also remove classes from the processed jar/folder. Use `<strip-class>` tag with regular expressions to match fully qualified class names to be removed.

```xml
<?xml version="1.0"?>
<rules>
    <strip-class>org\.unneeded\..*</strip-class>
    <strip-class>org\.useful\.Unused</strip-class>
    <methods>
        .
        .
        .
    </methods>
</rules>
```

If you do not want to transform a class, you can add a rule to skip it. To achieve this include regular expressions to match fully qualified class names in the rules in `<skip-class>` tag.

```xml
<?xml version="1.0"?>
<rules>
    <skip-class>org\.good\..*</skip-class>
    <skip-class>org\.useful\.LeaveIt</skip-class>
    <methods>
        .
        .
        .
    </methods>
</rules>
```

You can combine `skip-class` and `strip-class` rules, but remember that `skip-class` takes precedence over any other option.
 
For transformation to work, all of the classes referenced in transformed code should be available on the classpath. Your 
standard class path is included automatically, you can add additional folders and/or jar-files with `--classpath` option.

#### Hidden stuff ####

There is currently no way to tweak constructor stubbing. Mostly constructors have their bodies removed, if it fails for
any reason, then such constructor is left as is.

By default class version is not changed, but you can set new version for a generated classes with `--target X`, where
X is major Java version, i.e. 1, 2, 3, ... 8. 

No verification is done, so if the code uses StringBuilder class and you set version to 4, you will most likely get 
compilation errors. 

There are several more command-line parameters which affect how resulting classes are generated:

* `--strip-non-public` will remove all non-public methods and fields from the resulting class
* `--strip-fields` will remove all fields from class
* `--strip-final` will remove all final modifiers from classes and methods, so you can easily mock them with Mockito or similar tool
* `--help` will display more detailed information on command-line parameters


### Authors ###

* Oleksiy Voronin <me@ovoronin.info>
