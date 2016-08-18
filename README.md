# Slack pokebot
Get notifications when a nearby pokemon appear. 

# How to run

Configure in pokebot.properties.
```
git clone git@github.com:grumlimited/geocalc.git
cd geocalc
mvn install
cd ..
git clone git@github.com:joeljons/pokebot.git
cd pokebot
mvn install
java -cp lib/PokeGOAPI-library-all-0.4.0.jar;target/pokebot-1.0-SNAPSHOT-jar-with-dependencies.jar FindPokemons
```
