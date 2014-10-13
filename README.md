# Stubborn #

### What is this about ###

#### TL;DR ####

Stubborn is a command-line utility that cripples perfect Java classes into shadows of their former selves with stubbed methods. Might be useful for testing.

Current version is 0.0.1 and development is ongoing.

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
 
 1. Clone this repository and run mvn jar:jar. Use that jar file
 2. No there are no other options right now
 
### Usage ###

Stubborn uses Javassist to transform classes and rewrite methods, so you may want to familiarize yourself with
Javassist [syntax](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial2.html#intro)


Stubborn accepts folders and jar files as input and can produce folders and jar files as output. Magic happens in between. 
Rules by which magic happens are defined in rules file. 

#### Rules file ####

Rules file is an XML-file which defines how exactly should methods be stubbed.

Simplest rule file could be written as following:
```
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
```
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

You can skip any matcher. All supplied matchers are combined with AND operation. Obviously it makes no sense in using both ReturnType and Signature.

Put your Java code inside `body` tag. This code will be compiled and will be used instead original method body. All limitations of Javassist apply. 

If method is matched by multiple rules, Stubborn will fail immediately telling you which matchers collide. You can override this behavior with `--ignore-duplicate-matchers` parameter, in this case first matcher will be selected.

If method is not matched by any rules its body will be replaced with `return null;` for all methods which return objects and with `return 0;` for all methods which return numbers.

There are several more command-line parameters which affect how resulting classes are generated:

* `--strip-non-public` will remove all non-public methods and fields from the resulting class
* `--strip-fields` will remove all fields from class
* `--strip-final` will remove all final modifiers from classes and methods, so you can easily mock them with Mockito or similar tool
* `--help` will display more detailed information on command-line parameters

#### Limitations ####

Unfortunately, while being written in Java 8 itself, Stubborn does not support class-files generated with Java 8. 
This is a limitation of Javassist.

### Authors ###

* Oleksiy Voronin <me@ovoronin.info>
