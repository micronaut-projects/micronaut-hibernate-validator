package io.micronaut.configuration.hibernate.validator

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Value
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.context.exceptions.DependencyInjectionException
import jakarta.inject.Singleton
import org.hibernate.validator.constraints.URL
import spock.lang.Specification

class ValidatedConstructorBeanSpec extends Specification {

    void "a Hibernate constraint on a @Value constructor parameter is validated"() {
        given:
        ApplicationContext context = ApplicationContext.run(['c.url': 'test'])

        when:
        context.getBean(C)

        then:
        DependencyInjectionException e = thrown()
        e.cause instanceof BeanInstantiationException
        e.cause.message.contains('url - must be a valid URL')

        cleanup:
        context.close()
    }

    @Singleton
    static class C {
        final String url

        C(@URL @Value('${c.url}') String url) {
            this.url = url
        }
    }
}
