#!/usr/bin/env bash

set -ex

ARGS=" -h localhost -p 5445 -U elo elo"

dropdb $ARGS
createdb $ARGS
DATABASE_URL="postgres://elo@localhost:5445/elo" lein migratus migrate
