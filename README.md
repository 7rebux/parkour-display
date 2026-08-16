# ParkourDisplay

A Minecraft LabyMod addon, inspired by MPK Mod and Cyv Client, designed to
enhance gameplay with parkour-related features.

## Features

### Landing Blocks

- Set multiple landing blocks and calculate their landing offsets
- Two modes: `Land` (hitbox at the landing tick) and `Hit` (hitbox one tick
  after landing, e.g., for slime bounces)
- Personal best offsets per landing block, with optional chat output
- Configurable in-world highlighting (fill/outline color, outline thickness)

### Macros

- Record and play back macros (`savemacro`, `runmacro`)
- Absolute or relative rotation storage
- Optional smooth rotation interpolation between ticks during playback
- Servers can allow macro playback via the `parkourdisplay.macro`
  permission (disallowed by default)

### Runs & Splits

- Define a run with a start position, an end (e.g., a pressure plate) and any
  number of intermediate splits
- Tracks personal bests per split and shows split times in chat
- Save, load and list run files; landing blocks can be imported from run
  files
- Shows run finish offsets (how far you were from the finish on the last
  tick)
- Highlight split bounding boxes and the tick positions of your latest run
  in the world

### Server Integration

- Integration with the LabyMod Server API: servers can send run data
  (start, end, splits) directly to players
- Configurable load behavior for server-sent run data (always / confirm /
  never)

### Chat Logging

- Airtime and ground time durations for every jump
- Grinds (ceiling jump cancels)
- Landing block offsets and run splits
- Configurable decimal places and tick formatting (e.g. `02.750` instead of
  `55`)

## Widgets

- Air Time
- Ground Time
- Tier
- Velocity X, Y, Z
- Speed Vector
- Jump Coordinates X, Y, Z
- Jump Angle
- Landing Coordinates X, Y, Z
- Hit Coordinates X, Y, Z
- Hit Angle
- Hit Velocity
- Last Turn
- Last 45
- Last Input
- Last Timing
- Last Sidestep
- Preturn
- Second Turn
- Blips
- Last Landing Block Offsets
- Run Ground Time
- Run Splits

## Commands

All commands are available under `/parkourdisplay` or `/pd`.

### Landing Blocks

| Command    | Description                                                          |
|------------|----------------------------------------------------------------------|
| `addlb`    | Adds a new landing block                                             |
| `listlb`   | Lists all registered landing blocks                                  |
| `removelb` | Remove all or a single landing block                                 |
| `resetlb`  | Reset the personal best offsets for all or a single landing block    |

### Macros

| Command      | Description                                 |
|--------------|---------------------------------------------|
| `savemacro`  | Saves a macro with a given name             |
| `runmacro`   | Executes a specific or the last run macro   |
| `listmacros` | Lists all loaded macros                     |

### Runs & Splits

| Command          | Description                                    |
|------------------|------------------------------------------------|
| `setstart`       | Sets the run start position                    |
| `setend`         | Sets the run end position                      |
| `addsplit`       | Adds a new run split                           |
| `removesplit`    | Removes the latest run split                   |
| `resetsplits`    | Resets the personal bests for all run splits   |
| `savesplits`     | Saves the current run splits to a file         |
| `loadsplits`     | Loads a run split file                         |
| `listsplitfiles` | Lists all available run split files            |
| `clearrun`       | Resets run start, end and split data           |
