package com.dubbo_rpc.spi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 扩展加载器
 * 类似于 Java 的 ServiceLoader，但更简单好用
 */
public class ExtensionLoader<T> {

    // 扫描路径
    private static final String SERVICE_DIRECTORY = "META-INF/services/";
    // 缓存 ExtensionLoader 实例
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    // 缓存扩展类实现
    private final Map<String, Class<?>> extensionClasses = new ConcurrentHashMap<>();
    // 缓存扩展类实例
    private final Map<String, Object> cachedInstances = new ConcurrentHashMap<>();
    
    private final Class<T> type;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    // 获取 ExtensionLoader 实例
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        if (!type.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
        }

        ExtensionLoader<S> loader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            loader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    // 获取扩展类实例
    public T getExtension(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Extension name == null");
        }
        
        Object instance = cachedInstances.get(name);
        if (instance == null) {
            synchronized (cachedInstances) {
                instance = cachedInstances.get(name);
                if (instance == null) {
                    instance = createExtension(name);
                    cachedInstances.put(name, instance);
                }
            }
        }
        return (T) instance;
    }

    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("No such extension of name " + name);
        }
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + e.getMessage(), e);
        }
    }

    private Map<String, Class<?>> getExtensionClasses() {
        if (extensionClasses.isEmpty()) {
            synchronized (extensionClasses) {
                if (extensionClasses.isEmpty()) {
                    loadDirectory(extensionClasses);
                }
            }
        }
        return extensionClasses;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        String fileName = SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (Throwable t) {
            System.err.println("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").");
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final int ci = line.indexOf('#');
                if (ci >= 0) line = line.substring(0, ci);
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        String name = null;
                        int i = line.indexOf('=');
                        if (i > 0) {
                            name = line.substring(0, i).trim();
                            line = line.substring(i + 1).trim();
                        }
                        if (line.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(line);
                            if (!type.isAssignableFrom(clazz)) {
                                throw new IllegalStateException("Error when load extension class(interface: " +
                                        type + ", class line: " + clazz.getName() + "), class "
                                        + clazz.getName() + "is not subtype of interface.");
                            }
                            if (name != null && !name.isEmpty()) {
                                extensionClasses.put(name, clazz);
                            }
                        }
                    } catch (Throwable t) {
                        IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + resourceUrl + ", cause: " + t.getMessage(), t);
                        e.printStackTrace(); // 方便调试
                    }
                }
            }
        } catch (Throwable t) {
            // ignore
        }
    }
}
