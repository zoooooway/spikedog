package org.zoooooway.spikedog;

import org.apache.commons.cli.*;
import org.zoooooway.spikedog.classloader.WebAppClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author zoooooway
 */
public class Main {

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
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("spike-dog", options);
            System.exit(1);
            return;
        }

        if (war == null) {
            System.err.println("you need to specify war file");
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
                System.out.println("delete temp dir...");
                deleteDir(tempDir);
            } catch (IOException e) {
                e.printStackTrace();
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

        WebAppClassLoader classLoader = new WebAppClassLoader(classpath, libPath);
        // 扫描类路径和jar路径以获取servlet组件

        try (Stream<Path> walkStream = Files.walk(classpath)) {
            walkStream.forEach(path -> {
                if (Files.isRegularFile(path)
                        && path.endsWith(".class")
                        && !path.endsWith("module-info")
                        && !path.endsWith("package-info")) {

                    System.out.println(path);

                }
            });
        }

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
