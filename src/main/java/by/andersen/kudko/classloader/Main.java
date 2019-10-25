package by.andersen.kudko.classloader;

import by.andersen.kudko.classloader.classes.IJarSample;
import lombok.extern.log4j.Log4j2;

import static org.apache.logging.log4j.core.util.Loader.loadClass;

@Log4j2
public class Main {
    public static void main(String[] args) {
        log.error("Main START");
        // Создаем загрузчик
        OwnClassLoader jarClassLoader = new OwnClassLoader("target/classes/testJar.jar", "by.andersen.kudko.classloader.classes.impl");
// Загружаем класс

        Class<?> clazz;


// Создаем экземпляр класса

        IJarSample sample;


        try {
            clazz = jarClassLoader.loadClass("JarSample");
            sample = (IJarSample) clazz.newInstance();

            sample.demo("Test");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
