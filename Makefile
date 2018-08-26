staging:
	git push -v --force heroku-staging HEAD:master

prod:
	git push -v --force heroku-prod HEAD:master

pg:
	pgcli postgres://elo@localhost:5445/elo

pg-test:
	pgcli postgres://elo@localhost:5445/elo_test

backup:
	heroku pg:backups:capture -a fifa-elo
