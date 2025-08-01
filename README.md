<img width="466" height="102" alt="image" src="https://github.com/user-attachments/assets/2144fa01-10fc-447e-9679-b9d4d634389b" /> 

Battleship with a fully interactive GUI and the ability to play versus other human players over the internet or an AI. Made with Java/JavaFX.

## Overview
This repo contains two separate Maven projects that were originally created in IntelliJ IDEA, one for the client code and one for the server code (labeled BattleshipClient and BattleshipServer respectively).

While it is not exactly necessary to use IntelliJ IDEA to open and run these projects, it is recommended to minimize the chance of any errors or need for further manual configuration.

## Design and Usage
1. Open the BattleshipServer project in IntelliJ and run it with the Maven ```clean compile exec:java``` configuration. The server makes use of multithreading to handle multiple clients and synchronized blocks to  minimize concurrency issues.
2. Open the BattleshipClient project in IntelliJ and run it with the Maven ```clean compile exec:java``` configuration. The client has a full GUI designed with JavaFX, and upon connecting to the server, will allow you to queue up for a game with another player or versus an AI (AI games are also handled over the server, so the game cannot be played offline).
3. If you would like to test the multiplayer functionality on one computer, you must enable the "Allow multiple instances" option in the Maven configuration for the BattleshipClient project. Then, run the client project twice. This will allow you to play the game in two separate windows against yourself.
4. Place all of your ships in the desired arrangement, then hit the "Continue" button after choosing to play against a human or AI (must do this in both windows and choose "Human" if running two clients).
5. Play *Battleship*!

## Example
<img width="1600" height="1036" alt="image" src="https://github.com/user-attachments/assets/a8f47e26-9542-41ff-84a7-97bf4cfca7d1" />


