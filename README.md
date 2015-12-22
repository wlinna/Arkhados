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

<iframe src="https://player.vimeo.com/video/131966456" width="500" height="313" frameborder="0" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe> <p><a href="https://vimeo.com/131966456">Arkhados 0.5 trailer</a> from <a href="https://vimeo.com/user41505101">William Linna</a> on <a href="https://vimeo.com">Vimeo</a>.</p>

## Dependencies ##

In order to compile, you need stable release of jMonkeyEngine 3 and
shaderblowlib. You also need to meet all dependencies of jMonkeyEngine
3.

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
and Creative Commons Public Domain (CC0). This means that assets
that prohibit commercial use or derivation will not accepted in Arkhados.

**Replace one of the character models with better model** Currently
I'm using very low poly models for characters for various reasons. For
mage I couldn't find more detailed 3D model with armature animations ready.

I animated Venator myself because there was no other choice but the
quality of my animations is pretty bad. It would be nice if they were
replaced, perhaps one by one.

**Find or create good music** In Arkhados every hero uses different
  set of music. Currently Embermage has only 2 soundtracks.

You can find good music with good licenses from opengameart.org.
And remember to use ogg-format because mp3 is patent
encumbered and not supported

**Create nice portraits for heroes** It would be nice to select hero
by clicking on nice portrait instead of button with text like
"Embermage" or "Venator". Portraits could be used on user guides too.


### Code ###

**Improve fog of war** Arkhados has fog of war but it should be
  improved visually and perhaps optimized. One way to optimize it
  would be to make framebuffer smaller but so far I've failed to do
  that.

**Add nice trail effects**
Shotgun bullets would look lot nicer if they had some
kind of trails. Another nice use for trails would be to show nice red
trails when Venator swipes. I will add Pudge-like hook spell later so
trail system could be useful for that too. Maybe.

**In-game menu**
Allow players to access menu during game so that
player can configure keys, graphic settings etc. and immediately see
how it affects.

**Refactor code**

**Fix bugs**

**Dummy players**
It would make testing much faster if there was a nice way to add dummy
characters.

**Artificial Intelligence**

**Contribute on [ArkhadosNet](github.com/dnyarri/ArkhadosNet)**
Arkhados uses ArkhadosNet for networking. I'm sure it could be
improved and having some kind of tests for it would be very good.

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

**Design new hero** The more there are heroes, the better (to certain
limit of course). If you have good idea, you can design character and
give me a link. Just remember that I can't do everything. For example,
if there isn't decent model with rig and preferably working
animations, I wont add it, unless someone makes good enough model and
animations for it.

And the hero must be compatible with Arkhados' philosophy too so there
can't be any random elements like dodge chance and no instant hit
spells.

**Create custom UI style for Arkhados**
Currently Arkhados' UI uses Nifty GUI's default style. We should
change the font, add decorations, add nice background images to
different menus, change colours etc.

## License ##

Arkhados is mostly GPLv3 licensed. All source files contain license
header.
