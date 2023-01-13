# PaperFrame: Item frame visualization and manipulation

## Requirements

This plugin has been tested on a paper 1.18.2+ minecraft server. Spigot / bukkit implementations might work as well
since this project doesn't use any paper specific API (yet).

This plugin optionally depends on WorldEdit - if you have WorldEdit, highlighting and visibility/protection toggle
commands can act on a cuboid region selected in WorldEdit. See command usages for more information.

## Synopsis

### Notations

- `-abcd` is equivalent to `-a -b -c -d`.
- `< FLAGS >` indicates that the flags inside the flags are optional - i.e. the command is still valid without
  specifying these flags.
- `[ FLAG ]` indicates that the flag must be specified - i.e. the command cannot proceed without this flag.
- `[-a|-b]` indicates that you must specify either `-a` or `-b` but not both.

### `/framehighlight <-hpse> [-r radius|-w]`, `/fh <-hpse> [-r radius|-w]`

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

### `/frameshowhide <-01w>`, `/fsh <-01w>`

Hides or reveals item frames.

* `-w` causes the command to act on all frames within selected region from WorldEdit. Otherwise the command acts on the
  item frame that is currently attached to the block surface under the cursor.
* `-0` causes the command to reveal all item frames affected.
* `-1` causes the command to hide all item frames affected.
* When neither `-0` nor `-1` is specified, the command toggles visibility status for all item frames affected.

Requires `paperframe.showhide` permission - granted by default to everyone.

### `/frameconfigreload`, `/fcr`

Reloads plugin configuration from disk

Requires `paperframe.configreload` permission - granted by default to OP.

### `/frameprotect <-01w>`, `/fprot <-01w>`, `/fprotect <-01w>`

Protect or unprotect item frames. A protected frame cannot be destroyed by receiving damage or removal of supporting
blocks.

* `-w` causes the command to act on all frames within selected region from WorldEdit. Otherwise the command acts on the
  item frame that is currently attached to the block surface under the cursor.
* `-0` causes the command to remove protection for all item frames affected.
* `-1` causes the command to protect all item frames affected.
* When neither `-0` nor `-1` is specified, the command toggles protection status for all item frames affected.

### `/framestat`, `/fstat`

Reveals protection and visibility status.

### `/framemaps <-ga> id1 id2 id3....`, `/fmaps`

Spawn multiple item frames attached to the block you are looking at and place maps of specified ids into those frames.
Note that this will delete any item frames that already occupy that block surface if `-a` is not specified. Generated
frames will be hidden and protected by default.

This command cannot directly create in-the-air frames. A supporting block must exist for the frames to spawn, but the
supporting block can be removed once the stack has been created.

* Use `-g` to spawn maps with glowing item frames.
* Use `-a` to skip removal of existing frames and place new frames on top of existing ones.

## `/frameunmap`, `/funmap`

Delete the item frame stack under the cross hair. Frames doesn't need to be affixed to a block and may be in-the-air.