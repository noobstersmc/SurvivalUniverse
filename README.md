# SurvivalUniverse
AleIV's 1.15 survival universe plugin

Name of the plugin Survival Universe
Plugin for minecraft 1.15 and UP
 
To introduce this plugins i could describe it like a protection plugin, we will use the vanilla chunks in a minecraft world.
 
we will be able to define a chunk, top to button, to someone, so the people will be able to own chunks, and depending on who are you you can edit (when we say “edit” in this document we are refering to a hole list of events, break, put blocks, etc, etc)
 
also will be exceptions like allys, admins, helpers, that are some roles that we will se later, and depending which you are you can edit a some chunks or not
  
A. There are 3 kind of Chunks
 
A. “Normal Chunks” Vanilla chunks in a world that you can see throught the coords and F3 G, so we dont have to do anything with them, but we are going to califícate them as “normal chunk”, so everyone can edit them etc
 
B. “owned Chunks” Chunks that are not normal, because are owned by a specific player and have a list of events that only that player can do, (or his allys)
 
C. “city Chunks” Chunks that are inside a zone called “city” and doesnt have an owner, this are zones defined in the config of the plugin and have radius and center, working like the worldborder in vanilla but without the border obviously, the point is that will be events blocked inside the zone or not, depending if you have a permission to edit that zone









B. OWNED CHUNKS
 
When someone Own a chunk have to be saved in  a config of the plugin “Chunks.yml”

Player uuid
- coord. Of the chunk
- world where is the chunk

Note: save chunks inside the player because one player can have more than one chunk
 
/chunk own player (command to give a chunk to someone, so that person own that chunk)
 
/chunk delete (when someone own a chunk, and you stand in that chunk you can delete it from the player possession

/chunk check (to see who is the owner of the chunk where you are in) (just this command is global you don’t need permission to use it)

Notes;
 only can be 1 owner per chunk,
 
All commands are based on the chunk where the player that is executing the command is standing

only admins can use this commands inside their city:
 
/chunk own player
/chunk delete
 





C. CITYS
 
There has to be in the config of the plugin a file of config and a file where the chunks of the players are saved “Citys.yml”
 
-“Citys” this are the zones with radius and center that we mention before, has to specify in the config:
 
name of the city
-center of the city
-radius of the city
-world where the city are
-owners of the city
-helpers of the city
 
ADMIN: an admin of a city can do all events that are supposed to be blocked with other players, he bypass all them, also if you are not an owner of that city but you have OP or the permission city.edit you can edit it too
HELPER: a helper of a city can do same as admin except the admin commands and helpers cant edita n owned chunk inside of a city, just city chunks
 
owners can use to add/remove the helpers (only if are standing inside the city
 
/helper add player 
/helper remove player






/city (this command is global, everyone can use it without permission)

“&aCity %nameofthecity%: 
Admins:
- %player%
- %player%
Helpers:
- %player%
- %player%

D. FEATURE OF ALLYS
 
Everyone can use this 3 commands, to add allys, this means that your allys can edit everything in the chunks that you own and have the same permissions that you have, except that you can remove them add and see the list of them ad any time, and they will be allowed or not to do things in your chunks
 
This feature is saved in SQL and has a option to save it in another config file
 
/ally add player
/ally remove player
/ally list 













E. GENERAL NOTES
 
Add messages that you think are convenient to the commands, like when you give chunks to someone etc, just to know what i just do, and try always good orthography and good stetic
 
Please try as always to do everything the most optimized posible, the plugin is planned to support as many people as posible that minecraft allows,
 
Everything is saved in config files, just the allys feature is for SQL
 
You can add allys, helpers, everything also if they are offline, you cant add them if they dont exist in the list of nicks of mojang, this plugin is planned to be premium, also give and remove chunks from players and everything saved with uuid
 
An owned chunk can be inside a city zone, but that chunk cant be edited by helpers, just admins of the city and of the chunk or allys of that player

I want the sourcecode too, and please 

F. CANCELED EVENTS
 
There will be a list of canceled events in citys, (just people with permission can)

Also a list of events canceled in owned chunks , (just people with permission can)

Also  will be a list of canceled events in citys, (no one can do, just admins and OP and perm city.edit)



