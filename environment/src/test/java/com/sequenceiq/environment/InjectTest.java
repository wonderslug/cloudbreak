package com.sequenceiq.environment;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public class InjectTest {

    private static final Class<?>[] SUSPICIOUS_ANNOTATION_CLASSES = new Class[]{Service.class, Component.class};

    @Test
    public void testIfThereAreNoInjectsInServiceClasses() {
        Reflections reflections = new Reflections("com.sequenceiq.environment",
                new FieldAnnotationsScanner(),
                new TypeAnnotationsScanner(),
                new SubTypesScanner(),
                new MemberUsageScanner());
        Set<String> problematicClasses = new LinkedHashSet<>();

        reflections.getFieldsAnnotatedWith(Inject.class).forEach(field -> {
            try {
                boolean fieldInComponentOrServiceClass = Set.of(field.getDeclaringClass().getAnnotations())
                        .stream()
                        .anyMatch(InjectTest::isAnnotatedWithUnnecessaryClasses);
                if (fieldInComponentOrServiceClass) {
                    problematicClasses.add(field.getDeclaringClass().getName());
                }
            } catch (RuntimeException ignore) {
            }
        });

        Assert.assertTrue(String.format("Service and Component classes shouldn't use @Inject, private fields should've set through constructor injection."
                        + " %nAffected classes: %n%s", String.join("\n", problematicClasses)), problematicClasses.isEmpty());
    }

    private static boolean isAnnotatedWithUnnecessaryClasses(Annotation annotation) {
        for (Class<?> annotationClass : SUSPICIOUS_ANNOTATION_CLASSES) {
            if (annotation.annotationType().equals(annotationClass)) {
                return true;
            }
        }
        return false;
    }

}
