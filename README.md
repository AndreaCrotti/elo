# elo

[![CircleCI](https://circleci.com/gh/AndreaCrotti/elo/tree/master.svg?style=svg)](https://circleci.com/gh/AndreaCrotti/elo/tree/master)

Compute the Elo ranking from Fifa games.

## Usage

### Local development

Get it running locally:

- run `lein dev` in one terminal (or `cider-in` with Cider)
- in another terminal run `lein garden auto` (this will compile
  automatically all the CSS)
- run `docker-compose up -d` to fire up your local db
- the first time run `./setup_db.sh`, this will migrate your local
  databases and seed them with randomly generated data

### CSS

CSS is written using `garden`

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
