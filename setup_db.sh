#!/usr/bin/env bash

set -ex

DEV=" -h localhost -p 5445 -U elo elo"
TEST="-h localhost -p 5445 -U elo elo_test"

dropdb --if-exists $DEV; createdb $DEV

dropdb --if-exists $TEST; createdb $TEST

DATABASE_URL="postgres://elo@localhost:5445/elo" lein migratus migrate
DATABASE_URL="postgres://elo@localhost:5445/elo_test" lein migratus migrate

DATABASE_URL="postgres://elo@localhost:5445/elo" lein run -m elo.seed
