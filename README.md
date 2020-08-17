# Micronaut Hibernate Validator

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.configuration/micronaut-hibernate-validator.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.configuration%22%20AND%20a:%22micronaut-hibernate-validator%22)
[![Build Status](https://travis-ci.org/micronaut-projects/micronaut-hibernate-validator.svg?branch=master)](https://travis-ci.org/micronaut-projects/micronaut-hibernate-validator)


This project includes integration with Hibernate Validator for [Micronaut](http://micronaut.io).

## Documentation

See the [Documentation](https://micronaut-projects.github.io/micronaut-hibernate-validator/latest/guide) for the current docs.

See the [Snapshot Documentation](https://micronaut-projects.github.io/micronaut-hibernate-validator/snapshot/guide) for the current development docs.

## Snapshots and Releases

Snaphots are automatically published to [JFrog OSS](https://oss.jfrog.org/artifactory/oss-snapshot-local/) using [Github Actions](https://github.com/micronaut-projects/micronaut-hibernate-validator/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to JCenter and Maven Central via [Github Actions](https://github.com/micronaut-projects/micronaut-hibernate-validator/actions).

A release is performed with the following steps:

- [Publish the draft release](https://github.com/micronaut-projects/micronaut-hibernate-validator/releases). There should be already a draft release created, edit and publish it. The Git Tag should start with `v`. For example `v1.0.0`.
- [Monitor the Workflow](https://github.com/micronaut-projects/micronaut-hibernate-validator/actions?query=workflow%3ARelease) to check it passed successfully.
- Celebrate!
