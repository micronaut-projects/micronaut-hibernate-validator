package io.micronaut.configuration.hibernate.validator;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import io.micronaut.core.order.Ordered;
import jakarta.inject.Singleton;

@Singleton
@Requires(bean = MicronautHibernateValidator.class)
public class HibernateMessageBundle extends ResourceBundleMessageSource {
    public HibernateMessageBundle() {
        super("org.hibernate.validator.ValidationMessages");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
