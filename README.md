# Introduction

Breakoutbox (bob) is a Minecraft mod that lets you control external scripts through console commands.

Key features:

* Run external scripts from a command block and update the command block's success count with the script's return code.
* Supply arbitrary console commands from an external script's stdout.
* Pass world data as parameters to external scripts - player names and positions, target lists, scoreboard values.
* Limit the frequency with which each script can be called on a global and per-block basis.
* Limit the runtime of each external script. 

Disclaimer: this mod is intended for people who know what they're doing. Be very very sure about what you're doing if you're enabling this mod on a public server,
because it could easily take your server down if your commands are heavyweight and your permissions too permissive.

Before getting started it is strongly recommended that you run "/gamerule commandBlockOutput false" to avoid having
insane amounts of logging.

# Commands

## /bob list

Lists the names of all registered commands.


## /bob reload

Reloads the breakoutbox.cfg configuration file from disk.


## /bob run and variants 

* /bob run <command> (<arg1> <arg2> ...)
* /bob runtarget <command> <target selector> (<arg1> <arg2> ...)
* /bob runscoreboard <command> <scoreboard objective> <target selector> (<arg1> <arg2> ...)

The runtarget and runscoreboard variants make additional information available to the executing script. See below for more information.


# Command definitions

Define your commands in a file called `breakoutbox.cfg` in the root of your server directory. The file should look like this:

```
[testcommand]
path=C:\yourcommand.exe
runAsRegularPlayer=true
runAsOpPlayer=true
runAsCommandBlock=true
commandTimeoutMilliseconds=5000
parseOutput=true
globalRateLimitMilliseconds=100
blockRateLimitMilliseconds=1000
[anothercommand]
path=C:\anothercommand.exe
...
```

Each command is defined in brackets and should have at least a path defined. Everything else is optional and will revert to using defaults if not provided.

## path

The path of the executable to run, plus any arguments. Variables can be used here and will be substituted - see below.


## runAsRegularPlayer, runAsOpPlayer, runAsCommandBlock

Defines permissions for your command. If left unspecified commands will be runnable as op players and inside command blocks. 


## rate limits

globalRateLimitMilliseconds and blockRateLimitMilliseconds control how often a command can be run successively. For example, if the global rate limit for your
command is 1000 milliseconds, your command can only run once every second, regardless of how many player or command blocks invoke the command. blockRateLimitMilliseconds
is the same except it limits per command block that the command is invoked from. For example, a blockRateLimitMilliseconds of 1000 will allow a player to invoke the same
command from a different command block even if the 1000 milliseconds haven't passed yet.


## parseOutput

If set to true the stdout output of the specified script will be interpreted as console commands. This is false by default since you'd have to write your own
custom script to take advantage of this.

## commandTimeoutMilliseconds

The maximum amount of time this external command is allowed to run before it is forcefully killed.


# Variables

Any of the below variables used in a command's path will be substituted by world information on execution.


## $args

These are echoed as-is to the executing script.
Example: if your path is "C:\testcommand.exe $args", then calling "/bob run testcommand abcd" will execute the command "C:\testcommand.exe abcd". 


## $objective

When called with runscoreboard, this is the name of the objective passed in to the command.


## $src

Entity information about the player or command block executing the command.

For players the format is: (name,X,Y,Z,optional objective score if running with runscoreboard). See $targets for examples.


## $targets

Entity information about a list of specified targets when ran with runtargets or runscoreboard. Format is the same as $src.
Multiple entities are separated by a semicolon (;).

Example - targeting the player entity, no objective specified, will produce:
    `playername,1,2,3`

Example - targeting multiple entities, some of which are named, with an objective specified that some have a score for:
    `donkeh,352,71,-76,1;,351,72,-73;,355,72,-70`


# Command block interaction

When executing a breakoutbox command through a command block, the success count is changed to the return value of the program, normalized to a number between 0 and 15.
This can be used to drive a redstone comparator, so you can hook up world actions that depend on the program's return value.

If the command is rate-limited the command block will keep its previous success count value.


# Examples
- dynamic maps youtube video
- easy clocks integrated python script
- IoT light switch
- animal pressure plate trigger real life light
- simple echo script
