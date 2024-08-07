package org.zoooooway.spikedog.classloader;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * @author zoooooway
 */
class WebClassLoaderTest {

    @Test
    void testLoadClass() throws Exception {
        Path classpath = Path.of("F:\\development\\temp\\spike-dog\\WEB-INF\\classes");
        Path libPath = Path.of("F:\\development\\temp\\spike-dog\\WEB-INF\\lib");
        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(classpath, libPath);

        // load app class
        String classname0 = "org.zoooooway.servlet.TestServlet";
        Class<?> aClass = webAppClassLoader.loadClass(classname0);
        assert classname0.equals(aClass.getName());

        // load lib class
        String classname1 = "org.slf4j.helpers.BasicMarker";
        Class<?> aClass1 = webAppClassLoader.loadClass(classname1);
        assert classname1.equals(aClass1.getName());


        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            classLoader.loadClass(classname0);
        } catch (Exception e) {
            assert e instanceof ClassNotFoundException;
        }

        try {
            classLoader.loadClass(classname1);
        } catch (Exception e) {
            assert e instanceof ClassNotFoundException;
        }
    }
}
