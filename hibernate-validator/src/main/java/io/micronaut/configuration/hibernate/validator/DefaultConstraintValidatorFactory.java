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
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.inject.DisposableBeanDefinition;

import jakarta.inject.Singleton;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

/**
 * Default implementation of the {@link ConstraintValidatorFactory} interface that
 * retrieves validators from the bean context.
 *
 * @author James Kleeh
 * @since 1.1.0
 */
@Singleton
public class DefaultConstraintValidatorFactory implements ConstraintValidatorFactory {

    private final BeanContext beanContext;

    /**
     * Constructor.
     *
     * @param beanContext beanContext
     */
    public DefaultConstraintValidatorFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        try {
            return beanContext.createBean(key);
        } catch (NoSuchBeanException e) {
            return InstantiationUtils.instantiate(key);
        }
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        beanContext.findBeanDefinition(instance.getClass())
                .filter(DisposableBeanDefinition.class::isInstance)
                .ifPresent(bd -> ((DisposableBeanDefinition<ConstraintValidator<?, ?>>) bd).dispose(beanContext, instance));
    }
}
