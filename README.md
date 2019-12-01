# ruuvi-storage

Webserver for storing and rendering RuuviTag data parsed by https://github.com/kotio-home-automation/ruuvitag-server

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running tests

    lein test

## Running

To start a web server for the application, run:

    lein ring server

## Deploying

    lein ring uberjar

    PORT=11111 java -server -jar target/ruuvi-storage-0.1.0-standalone.jar

To enable alarm emails set from and to emails in environment variables.

    ALARM_EMAIL_FROM=ruuvi-storage@example.com ALARM_EMAIL_TO=mypersonal@address.com PORT=11111 java -server -jar target/ruuvi-storage-0.1.0-standalone.jar

## Caveats

There is no authentication or authorization implemented. One should use a separate gateway authenticator or firewall to secure this service.

## License

Copyright © 2019 Ari Paasonen

Distributed under the Eclipse Public License either version 2.0 or (at your option) any later version.
