# ruuvi-storage

Webserver for storing and rendering RuuviTag data parsed by https://github.com/kotio-home-automation/ruuvitag-server

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## Deploying

    lein ring uberjar

    PORT=11111 java -server -jar target/ruuvi-storage-0.1.0-standalone.jar

## License

MIT?

Copyright © 2019 Ari Paasonen
