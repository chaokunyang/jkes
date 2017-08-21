package com.timeyang.jkes.spring.jpa.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 存在问题：需要实例化bean容器，不便于测试
 *
 * @author chaokunyang
 */
class SpringClassUtils {

    /**
     * get classes annotated with specified type and reside in specified packageName(sub package)
     * @param annotation
     * @param packageName
     * @return
     */
    static Collection<Class> getClasses(Class<? extends Annotation> annotation, String packageName) {
        return loadClassesBasedBeanDefinition(findCandidateComponents(annotation, packageName));
    }

    /**
     * get classes match with specified regex and reside in specified packageName(sub package)
     * @param regex
     * @param packageName
     * @return
     */
    static Collection<Class> getClasses(String regex, String packageName) {
        return loadClassesBasedBeanDefinition(findCandidateComponents(regex, packageName));
    }

    private static Collection<Class> loadClassesBasedBeanDefinition(Set<BeanDefinition> beanDefinitions) {
        Collection<Class> classes = new ArrayList<>();

        for (BeanDefinition bean: beanDefinitions) {
            try {
                Class<?> clazz = Class.forName(bean.getBeanClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        return classes;
    }

    private static Set<BeanDefinition> findCandidateComponents(Class<? extends Annotation> annotation, String packageName) {
        // create scanner and disable default filters (that is the 'false' argument)
        // default filters looks for {@link Component @Component}, {@link Repository @Repository},{@link Service@Service}, and {@link Controller@Controller}
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);

        // add include filters which matches all the classes (or use your own)
        provider.addIncludeFilter(new AnnotationTypeFilter(annotation));

        // get matching classes defined in the package
        return provider.findCandidateComponents(packageName);
    }

    private static Set<BeanDefinition> findCandidateComponents(String regex, String packageName) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);

        // add include filters which matches all the classes (or use your own)
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(regex)));

        // get matching classes defined in the package
        return provider.findCandidateComponents(packageName);
    }

}
