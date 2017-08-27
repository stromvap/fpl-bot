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
This bot is currently running and posting messages to: https://rfantasypl.slack.com - join by clicking here: https://join.slack.com/t/rfantasypl/shared_invite/MTg1NTU2NDE1MzAxLTE0OTUxNjY4MTktNzQxYzlhYWZkYg

No Discord running as of yet.

## Roadmap
Here is a brief list of TODO's.

* Automatically adjust what the current gameweek is