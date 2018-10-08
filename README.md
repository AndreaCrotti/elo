# elo

[![CircleCI](https://circleci.com/gh/AndreaCrotti/elo/tree/master.svg?style=svg)](https://circleci.com/gh/AndreaCrotti/elo/tree/master)

Compute the Elo ranking from Fifa games.

## Usage

### Local development

This project uses `figwheel-main` for all the reloading magic, so you
can just it locally with `lein build` or simply `jack-in` with `cider`
which should do the right thing out of the box (see [.dir-locals file](./.dir-locals.el)).

### Deployment

You can deploy this to Heroku directly or build an uberjar and deploy it on any other platform.

### Migrations

You can run database migrations with:

    lein migratus migrate

## License

Copyright © 2018 Andrea Crotti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
