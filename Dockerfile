FROM openjdk:8-jre-alpine3.7 
LABEL name="Genesys Config Server" vendor="Nu Echo Inc." license="Private"
LABEL description="Genesys Config Server"
ARG MUTAGEN=build/releases/unix/mutagen

COPY ${MUTAGEN} .
COPY docker/environments.yml /root/.mutagen/

WORKDIR /

ENTRYPOINT ["mutagen"]
