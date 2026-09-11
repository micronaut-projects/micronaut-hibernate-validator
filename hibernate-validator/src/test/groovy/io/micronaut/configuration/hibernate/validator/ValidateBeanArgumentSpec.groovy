package io.micronaut.configuration.hibernate.validator

import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanResolutionContext
import io.micronaut.context.DefaultBeanResolutionContext
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.core.naming.Named
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ConstructorInjectionPoint
import io.micronaut.inject.FieldInjectionPoint
import io.micronaut.inject.InjectionPoint
import io.micronaut.inject.MethodInjectionPoint
import jakarta.inject.Singleton
import org.hibernate.validator.constraints.URL
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * Calls {@link MicronautHibernateValidator#validateBeanArgument} directly with each kind of
 * injection point, as core does while injecting a value.
 */
class ValidateBeanArgumentSpec extends Specification {

    private static final String INVALID_URL = 'test'
    private static final String VALID_URL = 'https://micronaut.io'

    @Shared
    @AutoCleanup
    ApplicationContext context = ApplicationContext.run()

    @Shared
    MicronautHibernateValidator validator = context.getBean(MicronautHibernateValidator)

    @Shared
    BeanDefinition<Target> definition = context.getBeanDefinition(Target)

    @Shared
    Argument<String> urlArgument = definition.constructor.arguments[0] as Argument<String>

    @Shared
    Argument<String> plainArgument = definition.constructor.arguments[1] as Argument<String>

    BeanResolutionContext resolutionContext = new DefaultBeanResolutionContext(context, definition)

    void "an argument without constraints is not validated"() {
        when:
        validator.validateBeanArgument(resolutionContext, field('url'), plainArgument, 0, INVALID_URL)

        then:
        noExceptionThrown()
    }

    void "a field value is validated against the constraints of the property"() {
        when:
        validator.validateBeanArgument(resolutionContext, field('url'), urlArgument, 0, VALID_URL)

        then:
        noExceptionThrown()

        when:
        validator.validateBeanArgument(resolutionContext, field('url'), urlArgument, 0, INVALID_URL)

        then:
        BeanInstantiationException e = thrown()
        e.message.contains('url - must be a valid URL')
    }

    void "a field without property constraints is not validated"() {
        when:
        validator.validateBeanArgument(resolutionContext, field('plain'), urlArgument, 0, INVALID_URL)

        then:
        noExceptionThrown()
    }

    void "a setter value is validated against the constraints of the property"() {
        when:
        validator.validateBeanArgument(resolutionContext, new NamedInjectionPoint(definition, 'setUrl'), urlArgument, 0, INVALID_URL)

        then:
        BeanInstantiationException e = thrown()
        e.message.contains('url - must be a valid URL')
    }

    void "an argument of a method that is not a setter is not validated"() {
        when:
        validator.validateBeanArgument(resolutionContext, new NamedInjectionPoint(definition, 'configure'), urlArgument, 0, INVALID_URL)

        then:
        noExceptionThrown()
    }

    void "a constructor argument is validated against the constraints of its parameter"() {
        given:
        ConstructorInjectionPoint<Target> constructor = definition.constructor

        when:
        validator.validateBeanArgument(resolutionContext, constructor, urlArgument, 0, VALID_URL)

        then:
        noExceptionThrown()

        when:
        validator.validateBeanArgument(resolutionContext, constructor, urlArgument, 0, INVALID_URL)

        then:
        BeanInstantiationException e = thrown()
        e.message.contains('must be a valid URL')
    }

    void "a constructor argument at an unknown index is not validated"() {
        when:
        validator.validateBeanArgument(resolutionContext, definition.constructor, urlArgument, 5, INVALID_URL)

        then:
        noExceptionThrown()
    }

    void "a constructor that cannot be found is not validated"() {
        given:
        ConstructorInjectionPoint<Target> constructor = Stub(ConstructorInjectionPoint) {
            getDeclaringBean() >> definition
            getArguments() >> ([Argument.of(Integer)] as Argument[])
        }

        when:
        validator.validateBeanArgument(resolutionContext, constructor, urlArgument, 0, INVALID_URL)

        then:
        noExceptionThrown()
    }

    void "a factory method argument is not validated"() {
        given:
        ConstructorInjectionPoint<Target> factoryMethod = Stub(ConstructorInjectionPoint, additionalInterfaces: [MethodInjectionPoint]) {
            getDeclaringBean() >> definition
            getArguments() >> definition.constructor.arguments
        }

        when:
        validator.validateBeanArgument(resolutionContext, factoryMethod, urlArgument, 0, INVALID_URL)

        then:
        noExceptionThrown()
    }

    private FieldInjectionPoint<Target, String> field(String name) {
        return Stub(FieldInjectionPoint) {
            getDeclaringBean() >> definition
            getName() >> name
        }
    }

    private static final class NamedInjectionPoint implements InjectionPoint<Target>, Named {
        private final BeanDefinition<Target> declaringBean
        private final String name

        NamedInjectionPoint(BeanDefinition<Target> declaringBean, String name) {
            this.declaringBean = declaringBean
            this.name = name
        }

        @Override
        BeanDefinition<Target> getDeclaringBean() {
            return declaringBean
        }

        @Override
        String getName() {
            return name
        }
    }

    @Singleton
    static class Target {
        @URL
        String url

        String plain

        Target(@URL String url, String plain) {
            this.url = url
            this.plain = plain
        }

        void configure(@URL String url) {
        }
    }
}
