# elo

[![CircleCI](https://circleci.com/gh/AndreaCrotti/elo/tree/master.svg?style=svg)](https://circleci.com/gh/AndreaCrotti/elo/tree/master)

[![Dependencies Status](https://versions.deps.co/AndreaCrotti/elo/status.svg)](https://versions.deps.co/AndreaCrotti/elo)

Compute the Elo ranking from Fifa games.

## Usage

### Local development

Run locally with `lein figwheel`

### Deployment

You can deploy this to Heroku directly or build an uberjar and deploy it on any other platform.

### Migrations

You can run database migrations with:

    lein migratus migrate

## Technical decisions

### Routing

Routing is done using Bidi and Accountant together.

## License

Copyright © 2018 Andrea Crotti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
