package com.timeyang.jkes.core.util;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * ClassUtils based Reflections lib
 *
 * @author chaokunyang
 */
public class ClassUtils {

    static Set<Class<?>> getClasses(String packageName) {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        return classes;
    }

    public static Set<Class<?>> getAnnotatedClasses(String packageName, Class<? extends Annotation> annotation) {

        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(annotation);
        return annotated;
    }

}