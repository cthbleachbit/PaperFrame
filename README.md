# PaperFrame: Item frame visualization and manipulation

## Requirements

This plugin has been tested on a paper 1.18.2+ minecraft server. Spigot / bukkit implementations might work as well
since this project doesn't use any paper specific API (yet).

This plugin optionally depends on WorldEdit - if you have WorldEdit, highlighting and visibility/protection toggle
commands can act on a cuboid region selected in WorldEdit. See command usages for more information.

## Synopsis

### Notations

- `-abcd` is equivalent to `-a -b -c -d`.
- `[ FLAGS ]` indicates that the flags inside the flags are optional - i.e. the command is still valid without
  specifying these flags.
- `FLAG` indicates that the flag must be specified - i.e. the command cannot proceed without this flag.
- `-a|-b` indicates that you may specify either `-a` or `-b` but not both.

### `/framehighlight [-hpse] [-r radius | -w]`

Toggles highlighting for frames near the user. White dust particles will outline item frames in a `20^3`,
or `(2 x radius)^3` if specified, cuboid space centered around the user.

* `-h` = highlight hidden frames.
* `-p` = highlight protected frames.
* `-s` = highlight overlapping frames created via WorldEdit / `/framemaps`
* `-e` = highlight frames that are empty.
* Use `-r radius` to specify the cubic radius of highlighting region. Default radius is 10 if omitted. Maximum distance
  allowed can be set server-wide in the configuration.
* Use `-w` to highlight frames within selection from WorldEdit. This option overrides `-r`.

Requires `paperframe.highlight` permission - granted by default to everyone.

### `/frameshowhide [--on|--off] [-w]`

Hides or reveals item frames.

* `-w` causes the command to act on all frames within selected region from WorldEdit. Otherwise the command acts on the
  item frame that is currently attached to the block surface under the cursor.
* `-0` causes the command to reveal all item frames affected.
* `-1` causes the command to hide all item frames affected.
* When neither `-0` nor `-1` is specified, the command toggles visibility status for all item frames affected.

Requires `paperframe.showhide` permission - granted by default to OP.

### `/frameconfigreload`, `/fcr`

Reloads plugin configuration from disk

Requires `paperframe.configreload` permission - granted by default to OP.

### `/frameprotect [--on|--off] [-w]`

Protect or unprotect item frames. A protected frame cannot be destroyed by receiving damage or removal of supporting
blocks.

* `-w` causes the command to act on all frames within selected region from WorldEdit. Otherwise the command acts on the
  item frame that is currently attached to the block surface under the cursor.
* `-0` causes the command to remove protection for all item frames affected.
* `-1` causes the command to protect all item frames affected.
* When neither `-0` nor `-1` is specified, the command toggles protection status for all item frames affected.

Requires `paperframe.protect` permission - granted by default to OP.

### `/framestat`

Reveals protection and visibility status.

Requires `paperframe.stat` permission - granted by default to everyone.

### `/framemaps [-ghpa] id1 id2 ....`

Spawn multiple item frames attached to the block you are looking at and place maps of specified ids into those frames.
Note that this will delete any item frames that already occupy that block surface if `-a` is not specified. Generated
frames will be hidden and protected by default.

This command cannot directly create in-the-air frames. A supporting block must exist for the frames to spawn, but the
supporting block can be removed once the stack has been created.

* `-g` = new frames should be glowing
* `-h` = new frames should be hidden
* `-p` = new frames should be protected
* `-a` = append only, otherwise existing frames in the occupying space will be removed

Requires `paperframe.mapstack` permission - granted by default to OP.

## `/frameunmap [-w]`

Delete the item frame stack under the cross hair. Frames doesn't need to be affixed to a block and may be in-the-air.

* `-w` = operate within WorldEdit selected cuboid instead of the frame under cursor

Requires `paperframe.mapstack` permission - granted by default to OP.

## `/frame2d {-n name | -w W -h H ids ...}`

Place tiles one by one onto a flat rectangle of frames with top-left corner under cursor. All frames must already exist.
Existing items in the frames will be deleted and replaced. All map ids will be validated prior to execution.

* `-n tileset-name` = retrieve geometry and map IDs from a tileset API server (not currently open source - coming soon)

Or specify grid width, height and map IDs manually. Map IDs may use the following format:

* `X` This exact map with ID exactly equal to X;
* `X:Y` Maps between X and Y inclusive in that order;
* `X+N` X and count upwards to X+N (inclusive);
* `X+-N` X+N and count downwards to X (inclusive).

Requires `paperframe.mapstack` permission - granted by default to OP.
