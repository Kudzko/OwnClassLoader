package by.andersen.kudko.classloader.classes.impl;

import by.andersen.kudko.classloader.classes.IJarSample;

public class JarSample implements IJarSample {
    public JarSample() {

        System.out.println("JarSample::JarSample()");

    }


    public void demo(String str) {

        System.out.println("JarSample::demo(String str)");
        System.out.println(str);

    }

}
