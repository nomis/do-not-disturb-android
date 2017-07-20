#!/bin/bash

# this script actually works on windows too! wow
# inkscape must be in the path

# launcher icon: 48 dp (48 inner)
# action bar:    32 dp (24 inner)
# notification:  24 dp (22 inner)

#               MDPI  HDPI    XHDPI   XXHDPI  XXXHDPI
#                1x   1.5x     2x       3x       4x
#               -------------------------------------
# LAUNCHER       48    72      96      144      192
# ACTION BAR     32    48      64       96      128
# NOTIFICATION   24    36      48       72       96

ICONS="ic_event_do_not_disturb ic_access_time ic_add ic_delete"
NOTIFICATIONS="ic_do_not_disturb_on ic_do_not_disturb_off"

function render {
	inkscape --file="$1.svg" --export-png="../../../res/drawable-$2/$1.png" --export-area-page --export-width=$3  --export-height=$4
}

for FILE in $ICONS; do
	echo processing icon: ${FILE}.svg
	render $FILE "mdpi" 32 32
	render $FILE "hdpi" 48 48
	render $FILE "xhdpi" 64 64
	render $FILE "xxhdpi" 96 96
	render $FILE "xxxhdpi" 128 128
done

for FILE in $NOTIFICATIONS; do
	echo Processing notification: ${FILE}.svg
	render $FILE "mdpi" 24 24
	render $FILE "hdpi" 36 36
	render $FILE "xhdpi" 48 48
	render $FILE "xxhdpi" 72 72
	render $FILE "xxxhdpi" 96 96
done
