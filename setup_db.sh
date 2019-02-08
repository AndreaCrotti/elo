#!/usr/bin/env bash

set -ex

DEV=" -h localhost -p 5445 -U byf byf"
TEST="-h localhost -p 5445 -U byf byf_test"

dropdb --if-exists $DEV; createdb $DEV

dropdb --if-exists $TEST; createdb $TEST

DATABASE_URL="postgres://byf@localhost:5445/byf" lein migratus migrate
DATABASE_URL="postgres://byf@localhost:5445/byf_test" lein migratus migrate

DATABASE_URL="postgres://byf@localhost:5445/byf" lein run -m byf.seed
