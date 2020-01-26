staging:
	git push -v --force heroku-staging HEAD:master

demo:
	git push -v --force demo HEAD:master

prod:
	git push -v --force heroku-prod HEAD:master

pg:
	pgcli postgres://byf@localhost:5445/byf

pg-test:
	pgcli postgres://byf@localhost:5445/byf_test

backup:
	heroku pg:backups:capture -a fifa-elo

migrate-local:
	DATABASE_URL=postgres://byf@localhost:5445/byf lein migratus migrate

migrate-test:
	DATABASE_URL=postgres://byf@localhost:5445/byf_test lein migratus migrate

db-graph:
	eralchemy -i postgres://byf@localhost:5445/byf -o byf_db.png


test:
	lein kaocha

.PHONY: test
