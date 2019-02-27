# elo

[![CircleCI](https://circleci.com/gh/AndreaCrotti/elo/tree/master.svg?style=svg)](https://circleci.com/gh/AndreaCrotti/elo/tree/master)
[![codecov](https://codecov.io/gh/AndreaCrotti/elo/branch/master/graph/badge.svg)](https://codecov.io/gh/AndreaCrotti/elo)

Platform to keep track of internal leagues.

Currently supported games:
- table tennis
- Fifa

### Local development

Get it running locally:

- run `docker-compose up -d` to fire up your local db
- the first time run `./setup_db.sh`, this will migrate your local
  databases and seed them with randomly generated data

- If you are using Emacs `M-x cider-jack-in-clj&cljs`
- type `(ir/go)` in the Clojure repl

### Deployment

You can deploy this to Heroku directly or build an uberjar and deploy it on any other platform.

### Migrations

You can run database migrations with:

    lein migratus migrate

## Contributing

Please see [CONTRIBUTING.md][1].

[1]: https://github.com/AndreaCrotti/elo/blob/master/CONTRIBUTING.md

## License

Copyright © 2018 Andrea Crotti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
