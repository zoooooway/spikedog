package org.zoooooway.spikedog.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author zoooooway
 */
public class WebAppClassLoader extends URLClassLoader {
    private final Logger log = LoggerFactory.getLogger(WebAppClassLoader.class);

    private final Path[] libJars;

    public WebAppClassLoader(Path classpath, Path libPath) throws IOException {
        super("WebAppClassLoader", createURLs(classpath, libPath).toArray(URL[]::new), ClassLoader.getSystemClassLoader());
        try (Stream<Path> libStream = Files.list(libPath)) {
            this.libJars = libStream.filter(path -> path.endsWith(".jar")).toArray(Path[]::new);
        }
        if (log.isDebugEnabled()) {
            log.debug("set classpath: {}", classpath);
            log.debug("set libPath: {}", Arrays.toString(libJars));
        }
    }

    /**
     * 创建用于加载类的路径url，classloader将基于这些url进行class查找
     *
     * @param classpath 应用程序类路径
     * @param libPath   应用程序依赖的jar文件路径
     * @return
     * @throws IOException
     */
    static List<URL> createURLs(Path classpath, Path libPath) throws IOException {
        List<URL> urls = new ArrayList<>();
        urls.add(classpath.toUri().normalize().toURL());

        // jar
        try (Stream<Path> libStream = Files.list(libPath)) {
            libStream.sorted()
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .forEach(path -> {
                        try {
                            urls.add(path.toUri().normalize().toURL());

                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return urls;
    }

}
