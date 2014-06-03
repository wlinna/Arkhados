# Arkhados #

Arkhados is fast paced multiplayer action PvP arena game where players
move around and try to kill each other by casting various spells.
Arkhados is entirely skill based, meaning that there is no automatic
targeting and no random elements like critical strike chance or dodge
chance.

Arkhados is very fast paced. There are no mana costs which makes
gameplay very fast. Games usually last less than 10 minutes.

Arkhados currently supports only deathmatch but teamplay will be added
later and I believe that it will be the most enjoyable game mode for
Arkhados.

It's programmed in Java using jMonkeyEngine 3.

## Dependencies ##

In order to compile, you need stable release of jMonkeyEngine 3. You
also need to meet all dependencies of jMonkeyEngine 3.

## ToDos and contribution ideas ##

If you want to contribute, great! Here's big list of ideas I'd love to
see in Arkhados. Some of there are something that I'm going to
implement myself anyway but I can't do all of them since I have to
focus on core features.

I will add more to this list and give more details on unexplained
items.

The list is unordered.

### Art 'n' assets ###

My artist's skills are severely limited so my power upon these ideas
are limited.

If you want to contribute or suggest art, remember that only
free/libre art is accepted, like Creative Commons Attributions (CC-BY)
and Creative Commons ShareAlike (CC-BY-SA). This means that assets
that prohibit commercial use or derivation will not accepted in Arkhados.

**Replace one of the character models with better model**

**Make nicer lava texture**

**Find or create good sound effect**

**Find or create good music for character**
I'm not actually sure if I wan't music to depend on character, arena
or nothing but I think music depending on character is the nicest
option.

You can find good music with good licenses from opengameart.org and
jamendo.com .

### Code ###

**Add nice trail effects**
Shotgun and machinegun bullets would look lot nicer if they had some
kind of trails. Another nice use for trails would be to show nice red
trails when Venator swipes. I will add Pudge-like hook spell later so
trail system could be useful for that too. Maybe.

**Ingame menu**
Very basic feature. Allow players to access menu during game so that
player can configure keys, graphic settings etc. and immediately see
how it affects.

**Commenting code**
Most of the Arkhados' code is uncommented. Commenting it is nice way
to make it more understandable for everyone.

**Refactor code**

**Fix bugs**

**Dummy players**
It would make testing much faster if there was a nice way to add dummy
characters.

**Artificial Intelligence**

**Testing server**
If I now want to change something on client side like particle effect
or HUD, I need to shutdown and start both client and server and then
join the game, select hero and finally start the game. It would be lot
nicer if I had simpler test server where player can join while game is
running. There would be no roundmanager, lobby or anything that makes
testing slower.

**Redesign and reprogram lobby**
NOTE: currently lobby means the screen where players go when they join
the game. Later it will the screen where player can chat with all
other players and see the game list.

Current lobby looks bad and lacks important features. For example,
players can't select teams, they can't see heroes graphically, chat is
very clumsy to use etc. The UI code is also very tied to logic. It is
clear that adding features to current lobby would not be good in long
term.

Here are some requirements for new lobby

- Ability to extend it when new requirements are added.
- Nice way to see portraits of heroes and selecting hero by clicking
  it. There should be possibility to get information about hero like
  spells, stats and perhaps even 3D model etc.
- Possibility to select game mode like deathmatch and team deathmatch.
- Changing map should be easy. There should be visible preview image
  for every map.


#### Implement some Quake 3 networking ideas ####

Arkhados is very fast paced and needs fast syncing. That's why we need
UDP. We also need to implement NAT hole punching because we can't
afford to host all games on central servers and forwarding ports is
hard and not always possible. That is one reason to support only ONE
protocol. Since we must have UDP, we have to drop TCP.

I've recently read articles explaining Quake 3 networking model and I
believe it fits Arkhados' need quite nicely. I think the most
important Quake 3 idea to implement is this:

 No true reliability. Instead, "reliable" messages like
 Chat-messages are sent repeatedly until they are confirmed. This
 saves time if message is somehow lost because we are  not waiting
 for confirmation.

Good links to understand Quake 3 networking better:

- http://trac.bookofhook.com/bookofhook/trac.cgi/wiki/Quake3Networking
- http://fabiensanglard.net/quake3/network.php
- http://www.ra.is/unlagged/network.html

This is something that I will do anyway, maybe this summer but it's
not the next thing I'm going to do.

#### Big project: Implement matchmaking server ####

Arkhados needs central server where player can meet and easily join
another games.

Since Arkhados project won't have money to actually host the games
themselves, matchmaking server should also pick one player to host the
game and help the host and other clients to connect to each
other. Doing this involves NAT hole punching.

One challenge for this matchmaking server and its NAT hole punching
feature is to actually find server / service that allows listening and
sending to custom UDP port.

### Design ###

**Design new map**

**Design new hero**

**Create custom UI style for Arkhados**
Currently Arkhados' UI uses Nifty GUI's default style. We should
change the font, add decorations, add nice background images to
different menus, change colours etc.

## License ##

Arkhados is mostly GPLv3 licensed. All source files contain license
header.
