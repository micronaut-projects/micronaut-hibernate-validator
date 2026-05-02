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
package io.micronaut.configuration.hibernate.validator

import io.micronaut.context.ApplicationContext
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.Validator
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.valueextraction.ExtractedValue
import jakarta.validation.valueextraction.ValueExtractor
import spock.lang.Specification

@MicronautTest(startApplication = false)
class CustomValueExtractorSpec extends Specification {

    @Inject
    Validator validator

    void "custom value extractor bean is registered"() {
        expect:
        validator.validate(new RequestBody(number: new Property<>(5))).size() == 1
        validator.validate(new RequestBody(number: new Property<>(10))).empty
    }

    void "custom value extractor bean is used during executable validation"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()
        Example example = applicationContext.getBean(Example)

        when:
        example.save(new RequestBody(number: new Property<>(5)))

        then:
        ConstraintViolationException e = thrown()
        e.constraintViolations.size() == 1
        String propertyPath = e.constraintViolations.first().propertyPath.toString()
        propertyPath.startsWith('save.')
        propertyPath.endsWith('.number')

        cleanup:
        applicationContext.close()
    }

    @Singleton
    static class Example {
        void save(@Valid RequestBody body) {
        }
    }

    static final class Property<T> {
        private final T value

        Property(T value) {
            this.value = value
        }

        T getValue() {
            return value
        }
    }

    static class RequestBody {
        @NotNull
        Property<@Min(10L) Integer> number
    }

    @Singleton
    static class PropertyValueExtractor implements ValueExtractor<Property<@ExtractedValue ?>> {
        @Override
        void extractValues(Property<?> originalValue, ValueExtractor.ValueReceiver receiver) {
            receiver.value(null, originalValue?.value)
        }
    }
}
