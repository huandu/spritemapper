Sprite Mapper
-------------

Sprite Mapper is a Java program which creates sprite maps (or sprite sheets) from a set of input images.

It's forked from http://opensource.cego.dk/spritemapper/. I add a few new features to generate sprite maps for OpenGL ES.

Usage
=====

Full document is located at http://opensource.cego.dk/spritemapper/doc.html. All build instructions and basic usages are not changed.

There is also a simplified version located at doc/index.html. I've updated this file for newly added option.

Changes in this fork:

1. Add a new option --reserve-border to reserve 1px transparent border among all sprites in a map.
2. Width and height of output sprite map file are *always* POT (power of two) value.
3. Change layout algorithm selection logic. If several layouters can generate same area size map file, new logic ensures the one generates smallest map width will be chosen.

License
=======

This software is licensed under GPLv3. See LICENSE for detail.
