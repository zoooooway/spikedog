package org.zoooooway.spikedog.classloader;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author zoooooway
 */
class ClassLoaderTest {

    @Test
    void test() throws Exception {
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

        try (Stream<Path> walkStream = Files.walk(classpath)) {
            walkStream.forEach(path -> {
                System.out.println(path);
                if (Files.isRegularFile(path)) {
                    String fileName = path.getFileName().toString();
                    if (!fileName.endsWith(".class")) {
                        return;
                    }

                    String classname = fileName.substring(0, fileName.length() - 6);
                    if ("module-info".equals(classname) || "package-info".equals(classname)) {
                        return;
                    }

                    String fullClassname = classpath.relativize(path).toString().replace(File.separator, ".");
                    try {
                        webAppClassLoader.loadClass(fullClassname.substring(0, fullClassname.length() - 6));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                }
                System.out.println("-------------");
            });
        }
        try (Stream<Path> walkStream = Files.walk(libPath)) {
            walkStream.forEach(path -> {
                System.out.println(path);
                if (Files.isRegularFile(path)
                        && path.endsWith(".class")
                        && !path.endsWith("module-info")
                        && !path.endsWith("package-info")) {

                    System.out.println("√");

                }

                System.out.println("-------------");
            });
        }

    }
}
