# PaperFrame: Item frame visualization and manipulation

## Requirements

This plugin has been tested on a paper 1.18.2+ minecraft server. Spigot / bukkit implementations might work as well
since this project doesn't use any paper specific API (yet).

Integration with WorldEdit is on the roadmap. What functionality should be exposed from WorldEdit is to be determined.

## Synopsis

### `/framehighlight [-hpse] [-r radius]`, `/fh [-hpse] [-r radius]`

Toggles highlighting for frames near the user. White dust particles will outline item frames in a `20^3`,
or `(2 x radius)^3` if specified, cuboid space centered around the user.

* `-h` = highlight hidden frames.
* `-p` = highlight protected frames.
* `-s` = highlight overlapping frames created via WorldEdit / `/framemaps`
* `-e` = highlight frames that are empty.
* Use `-r radius` to specify the cubic radius of highlighting region. Default radius is 10 if omitted. Maximum distance
  allowed can be set server-wide in the configuration.

Requires `paperframe.highlight` permission - granted by default to everyone.

### `/frameshowhide`, `/fsh`

Hides or reveals an item frame that is currently attached to the block surface under the cross hair.

Requires `paperframe.showhide` permission - granted by default to everyone.

### `/frameconfigreload`, `/fcr`

Reloads plugin configuration from disk

Requires `paperframe.configreload` permission - granted by default to OP.

### `/frameprotect [-0] [-1]`, `/fprot`, `/fprotect`

Takes `-0` for turning off protection or `-1` to turn on protection. A protected frame cannot be destroyed normally by
damage, removing the supporting block, or having a block occupying the same location.

### `/framestat`, `/fstat`

Reveals protection and visibility status.

### `/framemaps [-g] [-a] id1 id2 id3....`, `/fmaps`

Spawn multiple item frames attached to the block you are looking at and place maps of specified ids into those frames.
Note that this will delete any item frames that already occupy that block surface if `-a` is not specified. Generated
frames will be hidden and protected by default.

* Use `-g` to spawn maps with glowing item frames.
* Use `-a` to skip removal of existing frames and place new frames on top of existing ones.

## `/funmap`

Delete all item frames that attached to the block you are looking at.