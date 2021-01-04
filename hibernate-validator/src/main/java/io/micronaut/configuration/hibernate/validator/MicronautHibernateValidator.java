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

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotatedElementValidator;
import io.micronaut.inject.validation.BeanDefinitionValidator;
import io.micronaut.validation.validator.DefaultValidator;
import io.micronaut.validation.validator.ExecutableMethodValidator;
import io.micronaut.validation.validator.ReactiveValidator;
import io.micronaut.validation.validator.Validator;
import io.micronaut.validation.validator.ValidatorConfiguration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

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
public class MicronautHibernateValidator extends DefaultValidator implements Validator, ExecutableMethodValidator, ReactiveValidator, AnnotatedElementValidator, BeanDefinitionValidator {

    private final ValidatorFactory validatorFactory;
    private final jakarta.validation.Validator validator;

    /**
     * Default constructor.
     *
     * @param validatorFactory The validator factory
     * @param configuration The validator configuration
     */
    protected MicronautHibernateValidator(ValidatorFactory validatorFactory, @Nonnull ValidatorConfiguration configuration) {
        super(configuration);
        this.validatorFactory = validatorFactory;
        this.validator = validatorFactory.getValidator();
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validate(@Nonnull T object, @Nullable Class<?>... groups) {
        return validator.validate(object, groups);
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(@Nonnull T object, @Nonnull String propertyName, @Nullable Class<?>... groups) {
        return validator.validateProperty(
                object,
                propertyName,
                groups
        );
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(@Nonnull Class<T> beanType, @Nonnull String propertyName, @Nullable Object value, @Nullable Class<?>... groups) {
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

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validateReturnValue(@Nonnull T object, @Nonnull Method method, @Nullable Object returnValue, @Nullable Class<?>... groups) {
        return validator.forExecutables().validateReturnValue(
                object,
                method,
                returnValue,
                groups
        );
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorParameters(@Nonnull Constructor<? extends T> constructor, @Nonnull Object[] parameterValues, @Nullable Class<?>... groups) {
        return validator.forExecutables().validateConstructorParameters(
                constructor,
                parameterValues,
                groups
        );
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(@Nonnull Constructor<? extends T> constructor, @Nonnull T createdObject, @Nullable Class<?>... groups) {
        return validator.forExecutables().validateConstructorReturnValue(
                constructor,
                createdObject,
                groups
        );
    }

    @Override
    public <T> void validateBean(@Nonnull BeanResolutionContext resolutionContext, @Nonnull BeanDefinition<T> definition, @Nonnull T bean) throws BeanInstantiationException {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        final Class<?> beanType = bean.getClass();
        failOnError(resolutionContext, violations, beanType);
    }

    private <T> void failOnError(@Nonnull BeanResolutionContext resolutionContext, Set<ConstraintViolation<T>> errors, Class<?> beanType) {
        if (!errors.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Validation failed for bean definition [");
            builder.append(beanType.getName());
            builder.append("]\nList of constraint violations:[\n");
            for (ConstraintViolation<?> violation : errors) {
                builder.append("\t").append(violation.getPropertyPath()).append(" - ").append(violation.getMessage()).append("\n");
            }
            builder.append("]");
            throw new BeanInstantiationException(resolutionContext, builder.toString());
        }
    }
}
