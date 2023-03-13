# DeployApp Backend

## Development
Running `deployapp-backend` as part of [`deployapp-platform`](https://github.com/Transfusion/deployapp-platform#running-development) is recommended for a streamlined local development environment setup process.

## Environment
Any JDK supporting Java 11+. GraalVM CE 22.3.1, OpenJDK 11.0.18 is recommended.

Even though `deployapp-backend` does not currently use any GraalVM-specific features such as polyglot programming, GraalVM is used in https://deploy.plan.ovh for the sake of consistency with [`deployapp-storage-service`](https://github.com/Transfusion/deployapp-storage-service), which does use polyglot features, and the [performance benefits](https://www.graalvm.org/java/advantages/).

There are many scenarios where it is useful to run `deployapp-backend` independently, such as when trying to expose a local instance of `deployapp-backend` through a K8s cluster via [Telepresence](https://www.telepresence.io/) and `kubectl port-forward`.

Copy [`application-dev.yml`](https://github.com/Transfusion/deployapp-platform/blob/dev/deployapp-backend-config/application-dev.yml) to [`src/main/resources`](https://github.com/Transfusion/deployapp-backend/tree/main/src/main/resources) and edit it [accordingly](https://github.com/Transfusion/deployapp-platform#running-development), then run

```shell
JAVA_HOME=/path/to/jdk/Home SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

## Unit tests
TBD

## Integration tests

```shell
sh run_integration_tests.sh
```
spins up all the dependent services with `docker-compose`, then runs the tests.

Alternatively, the relevant services may be started with, for instance,
```shell
docker-compose -f docker-compose.test.yml up redis
```
and the desired tests in the test suite run with
```shell
./gradlew :test --tests io.github.transfusion.deployapp.external_integration.*
```
