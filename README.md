# Client-Server-Game
Group project poker game. My role involved network related backend server-side development. This includes Server.java, Lobby.java, Message.java, and PlayerThread.java.

# Texas Holdem Documentation
Group: Daemon Demons
Group Members:
- Noah Reiniger
- Ben Birchman
- Kiernan Connolly

## Execution and Usage Instructions
### Start Server
Option 1 (Recommended):
1. Copy all java files from the project into the linux lab servers (or any linux
machine). Navigate to folder in terminal
2. Type javac Server.java
3. Before running, type “ifconfig” and record the “inet addr” for clients to connect to.
For example, csslab3.uwb.edu’s IP is 10.155.176.28.
4. Type java Server 10000 (or any valid port#, we use 10000)
5. Anyone wanting to connect needs to login to our schools BIG-IP Edge client

Option 2 (more complicated): Import and clone Git Repository into eclipse; add port# as
command line arg in run configurations. Then run through eclipse.

## Running Client
Option 1:
1. Install and run the Jar file from
https://drive.google.com/drive/folders/1hpyPOItjzTzW56_j2s9Diwbh8Atjihs1 (requires
UW Google Drive login)
2. To connect to the server once the client is running you must be connected to the BIG-IP
Edge Client and know the IP address of where the server is running, and the port
number. An example command for connecting to the server inside the “Action” Window
within our application is “connect 1.1.1.1 10000 USERNAME” and shown above is how
to find the server IP address.

Option 2 (more complicated): Import and clone Git Repository into eclipse. Then run through
eclipse. You may need to install the Eclipse WindowBuilder in order to build the game which you
can find in the Eclipse Marketplace.

Note: Cannot be compiled on the Linux Labs because I believe that it doesn’t have all of the
required libraries installed there.

## How to Play Game
Once you are in the client, you must first connect to a server with this format:
connect <IP> <PORT> USERNAME (ex: connect 10.155.176.28 10000 Peng)
Now, you should be in the main lobby. Commands accessible to you are:
disconnect, creategame, joingame {ID}, scoreboard, listgames
Each of these commands are fairly self explanatory. The scoreboard will be empty unless a
game has been played or won. Listgames will not show any games for the first player that
connects, but this player can create a game themselves. Any subsequent players to connect to
the server will see that game and any others if they list games.
Once you create a game or join a game, the lobby is in STARTING mode until all players type
“ready”. The only commands available before the start of a game are “ready” and “exit”, which
returns you to the main lobby. Once the game starts (all players ready), no further players from
the main lobby can join, and lobby state will be RUNNING. you have these poker commands:
bet {chips}, fold, exit
bet {chips} will bet a certain amount of chips over the amount you have to call. Calling bet 0 will
call or check. It can only be done on your turn.
fold will fold your hand. It can be done outside of your turn.
From there on the game will act as Texas Hold’em poker. Player turns will rotate until one player
wins the pot, either by all other players folding or at showdown. If a player has 0 chips after the
end of a hand, they lose the game and are put into spectator mode. The showdown is different
from normal Texas Hold’em, as it only counts the high card and there are no side pots. An
important note, if a player exits or disconnects mid-game, they will automatically fold and the
game will continue.
Upon game completion, the lobby state will be set back to STARTING, and players can join from
the main lobby and type “ready” to prepare for the next game. The winner will be sent to the
scoreboard.


## Implementation
### Server side
Our server side code was made up of the Server, Lobby, Message, and PlayerThread class for
connection, and the PokerGame, Card, and PokerPlayer classes for the game logic.

### Server.java
Our server class opens a socket and listens on a specified port. When a new connection is
made, we move the new player to their own socket and create a new PlayerThread that
communicates with the client side player. If the player is the first player to connect, we create
the main lobby on a thread and add the player to that lobby.
Lobby.java and Message.java
Inside the lobby class, we manage all current players using a synchronized message queue
containing any messages sent from the client. Each time a new message is received, the
primary lobby loop iterates and the Message class parses the message and categorizes it with
the appropriate logic and response for the client. Some messages are only meant to respond to
the client who sent them (like listgames), while others should update all users (like a chat
message or game function).
The lobby class parses messages either as the main lobby or a game lobby (defined by an
enum). Within a game lobby, the state of the game (RUNNING or STARTING) also controls
what commands users have access to. A player for example cannot bet if the game hasn’t
started. If a player creates a new game, the lobby class opens a new lobby thread and transfers
that player to the new game lobby.
Finally, at the end of a message queue iteration, the lobby class cycles through each player and
updates their history, gamestate, and chat depending on what type of message was first
received.

### PlayerThread.java
The PlayerThread class runs in a loop to receive messages from the client side. We chose to
run each player in their own thread so as to not hang the server on a specific player waiting for
input.

### PokerGame.java
Stores and runs the poker game logic. Runs on a turn based system. The Lobby class calls
bet() and fold() functions to advance the state of the game, and getHistory() and isGameOver()
to record the output from the previous state changing function. If fold is called with the second
parameter true, it will remove the player from the game.

### PokerPlayer.java
Stores a single PokerPlayer, with associated variables and a PlayerThread for the username.
Can update the values with basic getters and setters.

### Card.java
Stores a card as a collection of enums. Can create a deck of cards which is a List.

## Client side
The client runs on three threads and is composed of three classes: Client.java, ClientUIThread,
and GameComposite.

### Client.java
This class contains the main for the Client side of our application and is responsible for:
- Starting the UI Thread
- Passing in the two Processing Queues that will be used for handling messages to and
from the Server
- Establishing the connection between the Server and the Client
- Processing messages from the Queued up by the UI that will be sent to the Server
- Queueing up Messages messages received from the user to be process by the UI

### ClientUIThread.java
This class is responsible for:
- Creating the window the contains the GameComposite
- Passing in the Processing Queues for processing the messages to and from the server

### GameComposite.java
This class:
- Is the UI in which the User interfaces with and Creates 3 sub-windows: The History,
Action, and Chat windows
- Translates the messages queued by the Client that were received from the Server and
Displays them to the User
- Formates and queues inputs from the user and queues them up for the Client


## Networking Features
### Register/Unregister
The player gets registered with an ID upon connecting to the server, as well as a username.
They carry this ID with them throughout each lobby they join. When they disconnect, the ID is
cleared from all lobbies.

### List Games
A player in the main lobby can retrieve a list of active games, along with that game’s state
(STARTING or RUNNING) and the number of players in that game. If a game is RUNNING, no
further players can join that game until somebody wins.

### Create Game
When a player creates a new game, a new lobby with state:GAME_LOBBY is created on a
separate thread and now contains that player in it’s player list. A game lobby is open until all
players leave or disconnect, upon which it closes.

### Join Game
A player can join any STARTING game lobby that they’ve seen. The player types joingame ID,
where ID is the number associated with the game lobby they want to join.

### Exit Game
A player can exit a running game in 2 ways; first they can type “exit” and return to the main
lobby, and second they can brute force disconnect by closing their application. Both are handled
by the server appropriately. Finally, a player can leave the main lobby by typing “disconnect”.


### Application Specific Protocol

Message:(Length in New Lines)
Type:(Message Type, i.e. Message, Gamestate, Chat, History, or Command)
(Optional Headers):(ex: User who sent the message (Only if Message type from Server))
(actual message body)

### Chat
We implemented chat functionality by having a message type=chat that updates a chat log for
each player in the lobby. Chat logs are separated by lobby.

### Global Scoreboard
Each time a game ends, the poker game returns the name of the winner and how many wins
they have, and updates the global scoreboard (synchronized list).

## Team Contribution
We divided the work of the project into three parts, the Client/UI application, the Server, and the
Game Logic. Kiernan Handled the Game Logic, Noah handled the Client, and Ben handled the
creation of the server.
