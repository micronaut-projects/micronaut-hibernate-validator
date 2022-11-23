plugins {
    id("io.micronaut.build.internal.hibernate-validator-module")
}
dependencies {
    annotationProcessor(mn.micronaut.graal)
    compileOnly(libs.glassfish.el)
    compileOnly(mn.micronaut.router)
    implementation(libs.hibernate.validator)
    implementation(mn.micronaut.validation)
    runtimeOnly(libs.glassfish.jakarta.el)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mnSerde.micronaut.serde.jackson)
}
