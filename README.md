# PaperFrame: Item frame visualization and manipulation

## Requirements

This plugin has been tested on a paper 1.18.2+ minecraft server. Spigot / bukkit implementations might work as well since this project doesn't use any paper specific API (yet).

Integration with WorldEdit is on the roadmap. What functionality should be exposed from WorldEdit is to be determined.

## Synopsis

### `/framehighlight [-h] [-r radius]`, `/fh [-h] [-r radius]`

Toggles highlighting for frames near the user. White dust particles will outline item frames in a `20^3`, or `(2 x radius)^3` if specified, cuboid space centered around the user.

* Use `-h` flag to only highlight hidden frames.
* Use `-r radius` to specify the cubic radius of highlighting region. Default radius is 10 if omitted. Maximum distance allowed can be set server-wide in the configuration.

Requires `paperframe.highlight` permission - granted by default to everyone.

### `/frameshowhide`, `/fsh`

Hides or reveals an item frame that is currently attached to the block surface under the cross hair.

Requires `paperframe.showhide` permission - granted by default to everyone.

### `/frameconfigreload`, `/fcr`

Reloads plugin configuration from disk

Requires `paperframe.configreload` permission - granted by default to OP.