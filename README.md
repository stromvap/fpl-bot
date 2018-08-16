# fpl-bot

A very simple Fantasy Premier League bot that can post:
* Live scoring (currently goals and assists)
* Price change warnings
* Price changes

It can post to Slack and Discord.

Live scoring and price changes are fetched from the API at fantasy.premierleague.com

Price change warnings are fetched from the API at fplstatistics.co.uk

## Technical description

The bot is written in Java 8 using Spring Boot 1.5.3 and Maven.

## Configuration

The configuration file is located at `src/main/resources/application.properties`.

Before the bot can run you need to update the configuration to your liking, especially the Slack Auth Token and the Discord Webhooks.

## How to run

`mvn clean package && java -jar target/fpl-bot-0.0.1-SNAPSHOT.jar`

## Currently running

The bot is currently posting messages to:
* **Discord - r/FantasyPL** - [Click here to join](https://discordapp.com/invite/GgcY5gK)

## Icons

When the bot posts goals it will expect team icons to be avaliable in Slack using :team_name: format. I can't add the images here since they are copyrighted but i can tell you how to get them.

### OSX
1. Download this pack or any other suitable ICO pack: http://www.iconarchive.com/show/english-football-club-icons-by-giannis-zographos.html
2. Install ImageMagick
3. Clean all the ICO file names with this bash command (ImageMagick cant handle spaces): `for f in *\ *; do mv "$f" "${f// /_}"; done`
4. Convert them from ICO to PNG: `for file in *.ico; do convert $file 128x128 new-$file.png; done`
5. Rename the outputted icon names to match what the bot expects: [See here](src/main/java/fpl/bot/live/score/FplOfficialLiveScoreFetcher.java#L114-L135)
6. Upload the icons to your Slack or Discord server

### UNIX
If you are running UNIX i think you can manage yourself, it will propably be similar to the OSX way

### Windows
 ¯\_(ツ)_/¯

## Roadmap
Here is a brief list of TODO's.

* Automatically adjust what the current gameweek is
* Only poll the live score API when actual matches are ongoing
* Listen for configuration changes on the fly
* Post a message when a game is about to start?
* Post final scores of matches?
* Post BPS and BP at end of a match?
