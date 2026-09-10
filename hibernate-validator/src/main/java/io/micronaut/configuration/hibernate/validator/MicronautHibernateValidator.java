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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Argument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.validation.validator.DefaultValidator;
import io.micronaut.validation.validator.ValidatorConfiguration;

import jakarta.inject.Singleton;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;

/**
 * Replaces Micronaut Validator with Hibernate backed implementation.
 *
 * @author graemerocher
 * @since 2.0.0
 */
@Singleton
@Primary
@Requires(property = ValidatorConfiguration.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
@Replaces(DefaultValidator.class)
public class MicronautHibernateValidator extends DefaultValidator {

    private final Validator validator;

    /**
     * Default constructor.
     *
     * @param validatorFactory The validator factory
     * @param configuration The validator configuration
     */
    protected MicronautHibernateValidator(ValidatorFactory validatorFactory,
                                          @NonNull ValidatorConfiguration configuration) {
        super(configuration);
        validator = validatorFactory.getValidator();
    }

    @NonNull
    @Override
    public <T> Set<ConstraintViolation<T>> validate(@NonNull T object, @Nullable Class<?>... groups) {
        return validator.validate(object, groups);
    }

    @NonNull
    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(@NonNull T object, @NonNull String propertyName, @Nullable Class<?>... groups) {
        return validator.validateProperty(
                object,
                propertyName,
                groups
        );
    }

    @NonNull
    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(@NonNull Class<T> beanType, @NonNull String propertyName, @Nullable Object value, @Nullable Class<?>... groups) {
        return validator
                    .validateValue(
                            beanType,
                            propertyName,
                            value,
                            groups
                    );
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        return validator.getConstraintsForClass(clazz);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return validator.unwrap(type);
    }

    @NonNull
    @Override
    public <T> Set<ConstraintViolation<T>> validateReturnValue(@NonNull T object, @NonNull Method method, @Nullable Object returnValue, @Nullable Class<?>... groups) {
        return validator.forExecutables().validateReturnValue(
                object,
                method,
                returnValue,
                groups
        );
    }

    @NonNull
    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorParameters(@NonNull Constructor<? extends T> constructor, @NonNull Object[] parameterValues, @Nullable Class<?>... groups) {
        return validator.forExecutables().validateConstructorParameters(
                constructor,
                parameterValues,
                groups
        );
    }

    @NonNull
    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(@NonNull Constructor<? extends T> constructor, @NonNull T createdObject, @Nullable Class<?>... groups) {
        return validator.forExecutables().validateConstructorReturnValue(
                constructor,
                createdObject,
                groups
        );
    }

    @Override
    public <T> void validateBean(@NonNull BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> definition, @NonNull T bean) throws BeanInstantiationException {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        final Class<?> beanType = bean.getClass();
        failOnError(resolutionContext, violations, beanType);
    }

    /**
     * Validates an injected value with Hibernate Validator. The inherited implementation looks the
     * constraint validators up in Micronaut's own registry, which knows nothing about Hibernate's
     * constraints such as {@code @URL}. Values injected into a field or through a property setter
     * are validated against the constraints of that property, constructor arguments against the
     * constraints of that constructor parameter. Other method arguments are left alone, as whole
     * bean validation never covered them.
     *
     * @param resolutionContext The resolution context
     * @param injectionPoint    The injection point
     * @param argument          The argument
     * @param index             The argument index
     * @param value             The value
     * @param <T>               The value type
     * @throws BeanInstantiationException if the value is invalid
     */
    @Override
    public <T> void validateBeanArgument(@NonNull BeanResolutionContext resolutionContext,
                                         @NonNull InjectionPoint injectionPoint,
                                         @NonNull Argument<T> argument,
                                         int index,
                                         @Nullable T value) throws BeanInstantiationException {
        AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
        if (!annotationMetadata.hasStereotype(Constraint.class) && !annotationMetadata.hasStereotype(Valid.class)) {
            return;
        }
        Class<?> beanType = injectionPoint.getDeclaringBean().getBeanType();
        Set<? extends ConstraintViolation<?>> violations;
        if (injectionPoint instanceof FieldInjectionPoint<?, ?> fieldInjectionPoint) {
            violations = validatePropertyValue(beanType, fieldInjectionPoint.getName(), value);
        } else if (injectionPoint instanceof ConstructorInjectionPoint<?> constructor && !(injectionPoint instanceof MethodInjectionPoint<?, ?>)) {
            violations = validateConstructorArgument(beanType, constructor, index, value);
        } else if (injectionPoint instanceof Named method && NameUtils.isSetterName(method.getName())) {
            violations = validatePropertyValue(beanType, NameUtils.getPropertyNameForSetter(method.getName()), value);
        } else {
            return;
        }
        failOnError(resolutionContext, violations, beanType);
    }

    private Set<? extends ConstraintViolation<?>> validatePropertyValue(Class<?> beanType, String propertyName, @Nullable Object value) {
        if (validator.getConstraintsForClass(beanType).getConstraintsForProperty(propertyName) == null) {
            return Collections.emptySet();
        }
        return validator.validateValue(beanType, propertyName, value);
    }

    private Set<? extends ConstraintViolation<?>> validateConstructorArgument(Class<?> beanType,
                                                                             ConstructorInjectionPoint<?> constructorInjectionPoint,
                                                                             int index,
                                                                             @Nullable Object value) {
        Argument<?>[] arguments = constructorInjectionPoint.getArguments();
        if (index < 0 || index >= arguments.length) {
            return Collections.emptySet();
        }
        Class<?>[] parameterTypes = new Class<?>[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            parameterTypes[i] = arguments[i].getType();
        }
        Constructor<?> constructor;
        try {
            constructor = beanType.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return Collections.emptySet();
        }
        // Only this argument is known yet, so validate it alone and keep its violations
        Object[] parameterValues = new Object[arguments.length];
        parameterValues[index] = value;
        return validator.forExecutables().validateConstructorParameters(constructor, parameterValues).stream()
            .filter(violation -> isViolationOfParameter(violation, index))
            .collect(Collectors.toSet());
    }

    private static boolean isViolationOfParameter(ConstraintViolation<?> violation, int index) {
        for (Path.Node node : violation.getPropertyPath()) {
            if (node.getKind() == ElementKind.PARAMETER) {
                return node.as(Path.ParameterNode.class).getParameterIndex() == index;
            }
        }
        return false;
    }

    private void failOnError(@NonNull BeanResolutionContext resolutionContext, Set<? extends ConstraintViolation<?>> errors, Class<?> beanType) {
        if (errors.isEmpty()) {
            return;
        }
        var builder = new StringBuilder("Validation failed for bean definition [")
            .append(beanType.getName())
            .append("]\nList of constraint violations:[\n");
        for (ConstraintViolation<?> violation : errors) {
            builder.append('\t').append(violation.getPropertyPath()).append(" - ").append(violation.getMessage()).append('\n');
        }
        builder.append(']');
        throw new BeanInstantiationException(resolutionContext, builder.toString());
    }
}
