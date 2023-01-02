# https://github.com/docker/for-mac/issues/5873
# if [ "${OSTYPE//[0-9.]/}" == "darwin" ]
# then
#   export DOCKER_BUILDKIT=0
# fi
# docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit --exit-code-from integration-tests integration-tests
# docker-compose -f docker-compose.test.yml down

if [ -z "$GITHUB_ACTION" ]
then
  echo "Running outside of GH actions"
  docker build -f Dockerfile-integration-test -t deployapp-integration-test .
else
  echo "Running in GH actions"
  docker buildx build -f Dockerfile-integration-test -t deployapp-integration-test \
  --cache-to type=gha,mode=max \
  --cache-from type=gha --load .
fi

COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1 docker-compose -f docker-compose.test.yml up --abort-on-container-exit integration-tests
docker-compose -f docker-compose.test.yml down
