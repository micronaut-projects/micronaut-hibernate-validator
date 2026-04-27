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

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.TypeHint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.valueextraction.ValueExtractor;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Properties;

/**
 * Provides a {@link ValidatorFactory} instance.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Factory
@Requires(classes = HibernateValidator.class)
@TypeHint(HibernateValidator.class)
public class ValidatorFactoryProvider {

    private final @Nullable MessageInterpolator messageInterpolator;
    private final @Nullable TraversableResolver traversableResolver;
    private final @Nullable ConstraintValidatorFactory constraintValidatorFactory;
    private final @Nullable ParameterNameProvider parameterNameProvider;
    private final ValueExtractor<?>[] valueExtractors;
    private final HibernateValidatorConfiguration configuration;

    @Inject
    public ValidatorFactoryProvider(@Nullable MessageInterpolator messageInterpolator,
                                    @Nullable TraversableResolver traversableResolver,
                                    @Nullable ConstraintValidatorFactory constraintValidatorFactory,
                                    @Nullable ParameterNameProvider parameterNameProvider,
                                    ValueExtractor<?>[] valueExtractors,
                                    HibernateValidatorConfiguration configuration) {
        this.messageInterpolator = messageInterpolator;
        this.traversableResolver = traversableResolver;
        this.constraintValidatorFactory = constraintValidatorFactory;
        this.parameterNameProvider = parameterNameProvider;
        this.valueExtractors = valueExtractors;
        this.configuration = configuration;
    }

    /**
     * Builds the {@link ValidatorFactory} and applies Micronaut-managed validator components.
     *
     * @param environment The Micronaut environment, if available
     * @return The configured {@link ValidatorFactory}
     */
    @Singleton
    @Requires(classes = HibernateValidator.class)
    ValidatorFactory validatorFactory(@Nullable Environment environment) {
        Configuration<?> validatorConfiguration = Validation.byDefaultProvider()
            .configure();

        validatorConfiguration.messageInterpolator(messageInterpolator == null ? new ParameterMessageInterpolator() : messageInterpolator);
        if (traversableResolver != null) {
            validatorConfiguration.traversableResolver(traversableResolver);
        }
        if (constraintValidatorFactory != null) {
            validatorConfiguration.constraintValidatorFactory(constraintValidatorFactory);
        }
        if (parameterNameProvider != null) {
            validatorConfiguration.parameterNameProvider(parameterNameProvider);
        }
        for (ValueExtractor<?> valueExtractor : valueExtractors) {
            validatorConfiguration.addValueExtractor(valueExtractor);
        }

        if (configuration.isIgnoreXmlConfiguration()) {
            validatorConfiguration.ignoreXmlConfiguration();
        }
        if (environment != null) {
            Properties config = environment.getProperty("hibernate.validator", Properties.class).orElse(null);
            if (config != null) {
                for (Map.Entry<Object, Object> entry : config.entrySet()) {
                    Object value = entry.getValue();
                    if (value != null) {
                        validatorConfiguration.addProperty(
                            "hibernate.validator." + entry.getKey(),
                            value.toString()
                        );
                    }
                }
            }
        }
        return validatorConfiguration.buildValidatorFactory();
    }
}
