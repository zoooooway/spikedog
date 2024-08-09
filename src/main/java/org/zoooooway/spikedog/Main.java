package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServlet;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.classloader.WebAppClassLoader;
import org.zoooooway.spikedog.connector.HttpConnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author zoooooway
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        String war = null;

        Options options = new Options();
        options.addOption(new Option("w", "war", true, "war file"));

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            war = line.getOptionValue("w");
        } catch (ParseException exp) {
            // oops, something went wrong
            LOG.error("Parsing failed.  Reason: ", exp);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("spike-dog", options);
            System.exit(1);
            return;
        }

        if (war == null) {
            LOG.error("you need to specify war file");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("spike-dog", options);
            System.exit(1);
            return;
        }

        // 将jar文件解压至临时目录

        // 创建临时目录
        Path tempDir = Files.createTempDirectory("spike-dog");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOG.info("delete temp dir: {}", tempDir);
                deleteDir(tempDir);
            } catch (IOException e) {
                LOG.error("delete temp dir failed", e);
            }
        }));

        Path classpath = tempDir.resolve("WEB-INF/classes");
        Files.createDirectories(classpath);
        Path libPath = tempDir.resolve("WEB-INF/lib");
        Files.createDirectories(libPath);

        // 解压
        try (JarFile jarFile = new JarFile(war)) {
            jarFile.stream().forEach(entry -> {
                if (!entry.isDirectory()) {
                    Path path = tempDir.resolve(entry.getName());
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        Files.createDirectories(path.getParent());

                        Files.copy(is, path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        }

        try (WebAppClassLoader classLoader = new WebAppClassLoader(classpath, libPath)) {

            // 扫描类路径和jar路径以获取servlet组件

            Set<Class<? extends Servlet>> servletSet = new HashSet<>();
            Set<Class<? extends Filter>> filterSet = new HashSet<>();
            Set<Class<? extends EventListener>> listenerSet = new HashSet<>();

            // classpath
            try (Stream<Path> walkStream = Files.walk(classpath)) {
                walkStream.forEach(path -> {
                    if (Files.isRegularFile(path) && isNeedLoadClass(path)) {
                        String fullClassname = getFullClassname(path, classpath);

                        collectServletComponent(classLoader, fullClassname, servletSet, filterSet, listenerSet);
                    }
                });
            }

            // jars
            try (Stream<Path> walkStream = Files.list(libPath)) {
                walkStream.forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        String fileName = path.getFileName().toString();
                        if (fileName.endsWith(".jar")) {
                            try (JarFile jarFile = new JarFile(path.toFile())) {
                                jarFile.stream().forEach(entry -> {
                                    if (!entry.isDirectory()) {
                                        // ch/qos/logback/classic/AsyncAppender.class
                                        String name = entry.getName();
                                        if (name.endsWith(".class")) {
                                            String fullClassname = name.replace("/", ".");
                                            fullClassname = fullClassname.substring(0, fullClassname.length() - 6);
                                            String classname = fullClassname.substring(fullClassname.lastIndexOf(".") + 1);
                                            if (!"module-info".equals(classname) && !"package-info".equals(classname)) {
                                                collectServletComponent(classLoader, fullClassname, servletSet, filterSet, listenerSet);
                                            }
                                        }
                                    }
                                });

                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    }
                });
            }


            LOG.info("Servlets: {}", servletSet);
            LOG.info("Filters: {}", filterSet);
            LOG.info("Listeners: {}", listenerSet);

            // todo 启动server
            HttpConnector connector = new HttpConnector(classLoader, classpath.getParent().getParent(),
                    servletSet, filterSet, listenerSet);
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
            httpServer.createContext("/", connector);
            httpServer.start();
            LOG.debug("start server!");
            for (; ; ) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOG.error("server interrupted!", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            httpServer.stop(60);
        }
    }

    @SuppressWarnings("unchecked")
    private static void collectServletComponent(WebAppClassLoader classLoader, String fullClassname, Set<Class<? extends Servlet>> servletSet, Set<Class<? extends Filter>> filterSet, Set<Class<? extends EventListener>> listenerSet) {
        try {
            Class<?> clazz = classLoader.loadClass(fullClassname);

            WebServlet ws = clazz.getAnnotation(WebServlet.class);
            if (ws != null && Servlet.class.isAssignableFrom(clazz)) {
                LOG.info("scanned @WebServlet: {}", clazz);
                servletSet.add((Class<? extends Servlet>) clazz);
            }
            WebFilter wf = clazz.getAnnotation(WebFilter.class);
            if (wf != null && Filter.class.isAssignableFrom(clazz)) {
                LOG.info("scanned @WebFilter: {}", clazz);
                filterSet.add((Class<? extends Filter>) clazz);
            }
            WebListener wl = clazz.getAnnotation(WebListener.class);
            if (wl != null && EventListener.class.isAssignableFrom(clazz)) {
                LOG.info("scanned @WebListener: {}", clazz);
                listenerSet.add((Class<? extends EventListener>) clazz);
            }
        } catch (ClassNotFoundException e) {
            LOG.warn("load class '{}' failed. class not found: {}", fullClassname, e.getMessage());

        } catch (NoClassDefFoundError e) {
            LOG.warn("load class '{}' failed. no class defined found: {}", fullClassname, e.getMessage());
        }
    }

    private static boolean isNeedLoadClass(Path path) {
        String fileName = path.getFileName().toString();
        if (!fileName.endsWith(".class")) {
            return false;
        }

        String classname = fileName.substring(0, fileName.length() - 6);
        if ("module-info".equals(classname) || "package-info".equals(classname)) {
            return false;
        }
        return true;
    }

    private static String getFullClassname(Path path, Path classpath) {
        String fullClassname = classpath.relativize(path).toString().replace(File.separator, ".");
        fullClassname = fullClassname.substring(0, fullClassname.length() - 6);
        return fullClassname;
    }

    static void deleteDir(Path p) throws IOException {

        try (Stream<Path> stream = Files.list(p)) {
            stream.forEach(c -> {
                try {
                    if (Files.isDirectory(c)) {
                        deleteDir(c);
                    } else {
                        Files.delete(c);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            Files.delete(p);
        }

    }
}
