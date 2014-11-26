package net.ninjacat.stubborn.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import net.ninjacat.stubborn.fixtures.Test1;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Sandbox for testing various possible replacements
 */
public class JavassistPoc {

    @Test
    public void shouldCreateNewObjectInstance() throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass cls = pool.get(Test1.class.getCanonicalName());
        CtMethod method = cls.getMethod("getPojo", "()Lnet/ninjacat/stubborn/test/Pojo;");
        method.setBody("return ($r)$type.newInstance();");
        cls.setName("Test2");

        Object instance = cls.toClass().getConstructor().newInstance();

        Method getPojo = instance.getClass().getMethod("getPojo");
        Object pojo = getPojo.invoke(instance);

        Assert.assertNotNull("Should return instance of POJO", pojo);
    }
}
