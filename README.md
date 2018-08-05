# elo

Compute the Elo ranking from Fifa games.

## Usage

Deploy this to Heroku.

## Import data from a Google Spreadsheet

Download a csv and run:

    lein run -m elo.db sample.csv

This will populate the local database with data from the CSV file.

## TDOO

- [ ] add some proper styliing
- [ ] add a user table (use email as PK or UIUDS)?
- [ ] eventually paginatate list of all the games
- [ ] fetch all possible list of teams and auto complete on input
- [ ] auto complete on player names
- [ ] set some defualt values in the spreadsheet
- [ ] add authentication using Google Apps to limit to a company

## License

Copyright © 2018 Andrea Crotti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
