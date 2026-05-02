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

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for Micronaut Hibernate Validator integration.
 */
@ConfigurationProperties("hibernate.validator")
public class HibernateValidatorConfiguration {

    private boolean ignoreXmlConfiguration = true;

    /**
     * Returns whether XML-based validator configuration should be ignored.
     * Subclasses overriding this method must preserve the binding semantics of the
     * {@code hibernate.validator.ignore-xml-configuration} property.
     *
     * @return {@code true} if XML configuration should be ignored
     */
    public boolean isIgnoreXmlConfiguration() {
        return ignoreXmlConfiguration;
    }

    /**
     * Updates whether XML-based validator configuration should be ignored.
     * Subclasses overriding this method must preserve binding behavior for the
     * {@code hibernate.validator.ignore-xml-configuration} property.
     *
     * @param ignoreXmlConfiguration {@code true} if XML configuration should be ignored
     */
    public void setIgnoreXmlConfiguration(boolean ignoreXmlConfiguration) {
        this.ignoreXmlConfiguration = ignoreXmlConfiguration;
    }
}

