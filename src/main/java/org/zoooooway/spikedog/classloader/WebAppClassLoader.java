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
    
    private final Path classpath;
    private final Path[] libPath;


    public WebAppClassLoader(Path classpath, Path[] libPath) throws IOException {
        super("WebAppClassLoader", createURLs(classpath, libPath).toArray(URL[]::new), ClassLoader.getSystemClassLoader());
        this.classpath = classpath;
        this.libPath = libPath;
        if (log.isDebugEnabled()) {
            log.debug("set classpath: {}", classpath);
            log.debug("set libPath: {}", Arrays.toString(libPath));
        }
    }

    /**
     * 为给定路径中的文件创建url
     *
     * @param classpath 应用程序类路径
     * @param libPath 应用程序依赖的jar文件路径
     * @return
     * @throws IOException
     */
    static List<URL> createURLs(Path classpath, Path[] libPath) throws IOException {
        List<URL> urls = new ArrayList<>();
        urls.add(classpath.toUri().toURL());
        
        // jar
        Arrays.stream(libPath).sorted().forEach(dir -> {
            try (Stream<Path> pathStream = Files.walk(dir)) {
                pathStream.sorted().forEach(path -> {
                    try {
                        urls.add(path.toUri().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        return urls;
    }

}
