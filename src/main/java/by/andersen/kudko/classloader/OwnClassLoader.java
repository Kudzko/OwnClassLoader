package by.andersen.kudko.classloader;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Log4j2
public class OwnClassLoader extends ClassLoader {
    private static String WARNING = "Warning : No jar file found." +
            "Packet unmarshalling won't be possible. Please verify your classpath";


    private Map<String, Class<?>> cache = new HashMap<String, Class<?>>();
    private String jarFileName;
    private String pacageName;

    public OwnClassLoader(String jarFileName, String packageName) {
        this.jarFileName = jarFileName;
        this.pacageName = packageName;

        cacheClasses();
    }

    /**
     * We should cache Classes when they downloads
     */

    private void cacheClasses() {
        try {

            JarFile jarFile = new JarFile(jarFileName);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                // It's better to validate classes during the loading
                if (validate(jarEntry, pacageName)) {

                    byte[] classData = loadClassData(jarFile, jarEntry);
                    if (classData != null) {
                        Class<?> currentClass = defineClass(stripClassName(normalize(jarEntry.getName())),
                                classData, 0, classData.length);

                        cache.put(currentClass.getName(), currentClass);
                        log.trace(cache);

                        log.info("== class " + currentClass.getName() + " loaded in cache");
                    }
                }

            }


        } catch (IOException e) {
            log.warn(e);
            log.warn(WARNING);

        }
    }

    private boolean validate(JarEntry jarEntry, String pacageName) {
        log.trace("Validate method called");
        log.trace("jarEntry name  " + jarEntry.getName());
        if ((jarEntry != null)
                && (pacageName != null)
                && match(normalize(jarEntry.getName()), pacageName)
                && (pacageName.length() > 0)) {
            log.trace("Validate method result: " + true);
            return true;
        } else {
            log.trace("Validate method result: " + false);
            return false;
        }
    }

    /**
     * Transforms name in filesystem to name of Class;
     * changes "/" to "."
     *
     * @param className
     * @return
     */
    private String normalize(String className) {
        String result = className.replace("/", ".");
        log.trace("Normalize method result: " + result);
        return result;
    }

    /**
     * Checks if className has the familiar name with package
     * and ends on ".class"
     *
     * @return
     */
    private boolean match(String className, String pacageName) {
        boolean result = className.startsWith(pacageName) && className.endsWith(".class");
        log.trace("Match method called: " + result);
        return result;
    }

    /**
     * Get file from set JarEntry
     *
     * @param jarFile
     * @param jarEntry
     * @return
     * @throws IOException
     */
    private byte[] loadClassData(JarFile jarFile, JarEntry jarEntry) throws IOException {
        long size = jarEntry.getSize();
        if (size == -1 || size == 0) {
            return null;
        }

        byte[] data = new byte[(int) size];
        InputStream in = jarFile.getInputStream(jarEntry);
        in.read(data);
        return data;
    }

    /**
     * class loading implementation
     *
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        log.trace("IN LOAD CLASS METHOD");
        Class<?> result = cache.get(name);

        // check if class is called by short name (without package)
        if (result == null) {
            result = cache.get(pacageName.concat(".").concat(name));
        }

        // delegate to extendLoader
        if (result == null) {
            result = super.findSystemClass(name);
        }

        log.info("== loadClass(" + name + ")");

        return result;
    }


    /**
     * Получаем каноническое имя класса
     *
     * @param className
     * @return
     */

    private String stripClassName(String className) {
        log.trace("Class name stripped");
        return className.substring(0, className.length() - 6);
    }

}
