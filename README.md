# elo

Compute the Elo ranking from Fifa games.

## Usage

Deploy this to Heroku.

## Import data from a Google Spreadsheet

Download a csv and run:

    lein run -m elo.db sample.csv ids_mapping.edn

This will populate the local database with data from the CSV file, where the CSV contains data in this format:

    24/07/2018 10:00:15,Player1,Player2,3,1,reporter@email,Manchester United,Bayern Munich

And the `ids_mapping.edn` file contains mapping between player names and their UUID in the database, for example:

    {"Player1" "d27e2a00-3cf3-4cf2-a0ae-0166bf36b1cd"}
    
So all the players have to be created before.

## TODO

- [ ] eventually paginatate list of all the games
- [ ] fetch all possible list of teams and auto complete on input
- [ ] auto complete on player names
- [ ] set some default values in the spreadsheet
- [ ] add authentication using Okta/Google Apps to limit to a company
- [ ] add a way to play around with the various factors that affect how the scores are computed dynamically from the UI
- [x] add some proper styling
- [x] add a user table (use email as PK or UUIDS)?
- [x] add a way to add a new player

## License

Copyright © 2018 Andrea Crotti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
