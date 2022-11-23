import org.gradle.internal.execution.caching.CachingState.enabled

plugins {
    id("io.micronaut.build.internal.bom")
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
