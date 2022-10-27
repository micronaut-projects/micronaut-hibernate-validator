/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.hibernate.validator;

import io.micronaut.context.BeanContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.DelegatingBeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import jakarta.inject.Singleton;

import javax.validation.ParameterNameProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link ParameterNameProvider} interface that.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Singleton
public class DefaultParameterNameProvider implements ParameterNameProvider {

    private static final Set<String> INTERNAL_CLASS_NAMES = CollectionUtils.setOf(Object.class.getName(), "groovy.lang.GroovyObject");
    private final BeanContext beanContext;

    /**
     * Constructor.
     *
     * @param beanContext beanContext
     */
    public DefaultParameterNameProvider(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public List<String> getParameterNames(Constructor<?> constructor) {
        Class<?> declaringClass = constructor.getDeclaringClass();
        if (INTERNAL_CLASS_NAMES.contains(declaringClass.getName())) {
            return doGetParameterNames(constructor);
        }
        return beanContext.getBeanDefinitions(declaringClass)
                .stream()
                .map(bd -> {
                    if (bd instanceof DelegatingBeanDefinition) {
                        return ((DelegatingBeanDefinition<?>) bd).getTarget();
                    }
                    return bd;
                })
                .distinct()
                .findFirst()
                .map(def -> Arrays.stream(def.getConstructor().getArguments()).map(Argument::getName).collect(Collectors.toList()))
                .orElseGet(() -> defaultParameterTypes(constructor.getParameterTypes()));
    }

    @Override
    public List<String> getParameterNames(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (INTERNAL_CLASS_NAMES.contains(declaringClass.getName())) {
            return doGetParameterNames(method);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        Optional<? extends ExecutableMethod<?, Object>> executableMethod = beanContext.findExecutableMethod(declaringClass, method.getName(), parameterTypes);
        return executableMethod.map(m ->
                Arrays.stream(m.getArguments()).map(Argument::getName).collect(Collectors.toList())
        ).orElseGet(() -> defaultParameterTypes(parameterTypes));
    }

    /**
     * Add the parameter types to a list of names.
     *
     * @param parameterTypes parameterTypes
     * @return list of strings
     */
    protected List<String> defaultParameterTypes(Class<?>[] parameterTypes) {
        List<String> names = new ArrayList<>(parameterTypes.length);
        for (int i = 0; i < parameterTypes.length; i++) {
            names.add("arg" + i);
        }
        return names;
    }

    private List<String> doGetParameterNames(Executable executable) {
        Parameter[] parameters = executable.getParameters();
        List<String> parameterNames = new ArrayList<>(parameters.length);

        for (Parameter parameter : parameters) {
            parameterNames.add(parameter.getName());
        }

        return Collections.unmodifiableList(parameterNames);
    }
}
