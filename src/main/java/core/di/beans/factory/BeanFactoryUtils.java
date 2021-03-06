package core.di.beans.factory;

import core.annotation.Inject;
import core.annotation.PostConstruct;
import core.annotation.Qualifier;
import core.mvc.tobe.MethodParameter;
import core.util.ReflectionUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanFactoryUtils {

    private static final ParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    public static Object[] getParameters(BeanFactory beanFactory, Executable executable) {
        MethodParameter[] methodParameters = getMethodParameters(executable);

        Object[] parameters = new Object[methodParameters.length];

        for (int i = 0; i < methodParameters.length; i++) {
            String beanName = getBeanName(methodParameters[i]);
            parameters[i] = beanFactory.getBean(beanName, methodParameters[i].getType());
        }

        return parameters;
    }

    private static String getBeanName(MethodParameter methodParameter) {
        String name = methodParameter.getType().getName();
        Qualifier qualifier = methodParameter.getAnnotation(Qualifier.class);

        return qualifier == null ? name : qualifier.value();
    }

    private static MethodParameter[] getMethodParameters(Executable executable) {
        MethodParameter[] methodParameters = new MethodParameter[executable.getParameters().length];
        String[] parameterNames = getParameterNames(executable);
        Parameter[] parameters = executable.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            methodParameters[i] = new MethodParameter(executable, parameters[i].getType(), parameters[i].getAnnotations(), parameterNames[i]);
        }

        return methodParameters;
    }

    private static String[] getParameterNames(Executable executable) {
        if(executable instanceof Method) {
            return nameDiscoverer.getParameterNames((Method) executable);
        } else if(executable instanceof Constructor) {
            return nameDiscoverer.getParameterNames((Constructor<?>) executable);
        } else {
            throw new IllegalArgumentException("parameter executable must be a constructor or method");
        }
    }

    public static void injectField(BeanFactory beanFactory, Object instance, Field field) {
        try {
            String beanName = ReflectionUtils.getFieldBeanName(field);
            Object value = beanFactory.getBean(beanName, field.getType());
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new BeanInstantiationException(instance.getClass(), "illegal access '" + field.getName() + "'", e);
        }
    }

    public static Set<Field> findInjectFields(Class<?> targetClass) {
        return Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .collect(Collectors.toSet());
    }

    public static void invokePostConstructor(BeanFactory beanFactory, Object instance, Method method) {
        try {
            Object[] parameters = BeanFactoryUtils.getParameters(beanFactory, method);
            method.invoke(instance, parameters);
        } catch (IllegalAccessException e) {
            throw new BeanInstantiationException(method, "Cannot access method '" + method.getName() + "' is it public?", e);
        } catch (InvocationTargetException e) {
            throw new BeanInstantiationException(method, "Method threw exception", e.getTargetException());
        }
    }

    public static Set<Method> findPostConstructMethods(Class<?> targetClass) {
        return Arrays.stream(targetClass.getMethods())
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .collect(Collectors.toSet());
    }

    public static Constructor<?> findInjectController(Class<?> targetClass) {
        return Arrays.stream(targetClass.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .findAny()
                .orElseGet(() -> findPrimaryConstructor(targetClass));
    }

    private static Constructor<?> findPrimaryConstructor(Class<?> targetClass) {
        try {
            return targetClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new BeanInstantiationException(targetClass, "Constructor with @Inject not Found");
        }
    }
}
