plugins {
    id("io.micronaut.build.internal.hibernate-validator-module")
}

dependencies {
    annotationProcessor(mn.micronaut.graal)
    annotationProcessor(mn.micronaut.inject.java)
    compileOnly(mn.micronaut.router)
    api(libs.hibernate.validator)
    implementation(mn.micronaut.inject)
    implementation(mnValidation.micronaut.validation)

    testAnnotationProcessor(mnValidation.micronaut.validation.processor)
    testCompileOnly(mnValidation.micronaut.validation.processor)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mn.micronaut.http.server.netty)
    testRuntimeOnly(mn.snakeyaml)
    testImplementation(mnSerde.micronaut.serde.jackson)
}
