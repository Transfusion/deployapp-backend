# https://github.com/docker/for-mac/issues/5873
# if [ "${OSTYPE//[0-9.]/}" == "darwin" ]
# then
#   export DOCKER_BUILDKIT=0
# fi
# docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit --exit-code-from integration-tests integration-tests
# docker-compose -f docker-compose.test.yml down

docker build -f Dockerfile-integration-test -t deployapp-integration-test .
docker-compose -f docker-compose.test.yml up integration-tests  --abort-on-container-exit
docker-compose -f docker-compose.test.yml down