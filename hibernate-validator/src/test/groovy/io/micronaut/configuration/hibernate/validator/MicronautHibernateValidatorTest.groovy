package io.micronaut.configuration.hibernate.validator

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class MicronautHibernateValidatorTest extends Specification {

    @Inject
    EmbeddedApplication application

    def testItWorks() {
        expect:
            application.isRunning()
    }

}
