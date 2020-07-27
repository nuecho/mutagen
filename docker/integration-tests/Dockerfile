FROM node:8
LABEL name="Mutagen Acceptance Test Executor" vendor="Nu Echo Inc." 

# OpenJDK packages for Debian 8 "jessie" are located in backports repo
# https://xmoexdev.com/wordpress/installing-openjdk-8-debian-jessie/
RUN echo 'deb http://deb.debian.org/debian jessie-backports main' \
      > /etc/apt/sources.list.d/jessie-backports.list

RUN apt-get update \
  && apt-get install --no-install-recommends -y -t jessie-backports openjdk-8-jre=8u171* \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# Install wait-for-it (https://github.com/vishnubob/wait-for-it)
RUN curl -fsSL --compressed "https://raw.githubusercontent.com/vishnubob/wait-for-it/db049716e4/wait-for-it.sh" -o /usr/local/bin/wait-for-it.sh \
  && chmod 0755 /usr/local/bin/wait-for-it.sh

ENV WAIT_TIMEOUT 180
ENV CONFIG_SERVER_HOST configserver
ENV CONFIG_SERVER_PORT 2020

WORKDIR /usr/src/app

RUN npm install -g jest@22.4.4
RUN npm install shelljs@0.8.2 wait-on@2.1.0

COPY entrypoint.sh jest.config.js ./
RUN chmod +x ./entrypoint.sh

ENTRYPOINT [ "./entrypoint.sh" ]
CMD [ "--ci", "--colors" ]
