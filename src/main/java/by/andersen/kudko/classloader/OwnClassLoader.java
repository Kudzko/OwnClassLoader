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
            log.error("!!!!!!!!!!!!!!!!!!");
            JarFile jarFile = new JarFile(jarFileName);
            log.error("?????????????????????????");
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                // It's better to validate classes during the loading
                if (validate(jarEntry, pacageName)) {
                    log.error("after validation");
                    byte[] classData = loadClassData(jarFile, jarEntry);
                    if (classData != null) {
                        Class<?> currentClass = defineClass(stripClassName(normalize(jarEntry.getName())),
                                classData, 0, classData.length);
                        log.trace(jarEntry.getName());
                        log.trace(normalize(jarEntry.getName()));
                        cache.put(currentClass.getName(), currentClass);

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
        log.error("Validate method");
        log.error(jarEntry);

        if ((jarEntry != null)
                && (pacageName != null)
                && match(normalize(jarEntry.getName()), pacageName)
                && (pacageName.length() > 0)) {
            return true;
        } else {
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
        return className.replace("/", ".");
    }

    /**
     * Checks if className has the familiar name with package
     * and ends on ".class"
     *
     * @return
     */
    private boolean match(String className, String pacageName) {
        return className.startsWith(pacageName) && className.endsWith(".class");
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
        log.warn("IN LOAD CLASS METHOD");
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

        return className.substring(0, className.length() - 6);
    }

}
