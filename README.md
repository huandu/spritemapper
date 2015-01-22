Sprite Mapper 2
===============

SpriteMapper can pack several image files and/or directories into one png file with minimum image dimension. It can write Zwoptex compatible metadata file (plist format) for sprite map.

Quick Start
-----------

Use Sprite Mapper in following 3 steps.

Step 1: Download source code from GitHub.

`wget https://github.com/huandu/spritemapper/archive/master.zip`

Step 2: Unzip source code and run `ant` in code dir to build it.

```
$ unzip spritemapper-master.zip
$ cd spritemapper-master
$ ant
```

Compiled jar file is located in `./dist/SpriteMapper.jar`.

Step 3: Make your first sprite map with Sprite Mapper. 

```
java -jar dist/SpriteMapper.jar \
	--zwoptex=my_first.plist --out=my_first.png \
	--max-width=1024 --pot-size \
	path/to/your/sprites/dir
```

A short explanation to above command: It sets sprite map's name to `my_first.png` and its metadata name to `my_first.plist`. Sprite map's max width is 1024. Its output image width and height are guaranteed to be power of two value. Source images come from `path/to/your/sprites/dir`.

If you have any trouble, don't forget to read help message.

```
java -jar dist/SpriteMapper.jar
```

Full Introduction
-----------------

Sprite mapping is a technique of grouping many small images used in games or websites into one larger image in order to minimize the number of web requests needed to fetch all graphics and in the same time maybe minimize the file size. The result is a sprite map (also called sprite sheet, texture map or texture atlas).

A couple of programs exists for this purpose, for instance [Zwoptex](http://www.zwopple.com/zwoptex/) and [TexturePacker](http://www.codeandweb.com/texturepacker). Both of these unfortunately was closed source and not easy to have in your build process. Also Zwoptex only works on Mac.

As time goes by, Zwoptex and TexturePacker are getting stronger. Currently (late Aug, 2013), both softwares have CLI tool chains and can work on both Mac and Windows. However, they are still closed source commercial software. Sprite Mapper still can be a perfect replacement if you just need sprite map related feature.

This is where SpriteMapper comes into the picture.

**Sprite Mapper Features**

SpriteMapper contains many of the same features as Zwoptex and TexturePacker but it also has a couple of unique ones:

* Built in Java, so it runs everywhere.
* Implements most algorithms and heuristics mentionend in [this paper](http://clb.demon.fi/files/RectangleBinPack.pdf).
* SpriteMapper will run all algorithms and heuristics on the input and choose the one that does the job best.
* Supports rotation of sprites.
* Supports trimming away transparent edges.
* Supports adding spacing and border to sprites.
* Exports coordinate lists to Zwoptex 2 format used by [LimeJS](http://www.limejs.com/), [cocos2d](http://www.cocos2d-iphone.org/), [cocos2d-x](http://www.cocos2d-x.org/), [Corona](http://www.anscamobile.com/), [Sparrow](http://www.sparrow-framework.org/) and [LibGDX](http://code.google.com/p/libgdx/). More formats can easily be added if needed.
* Contains an Ant task to allow you to easily integrate with your `ant` based build environment.
* Contains a command line interface to allow you to easily integrate with all build environments.
* Released under an OpenSource license.

**Use in command line**

Sprite Mapper can run from the command line. The jar archieve contains a default main class in its manifest file so running it is easy.

```
$ java -jar SpriteMapper.jar
SpriteMapper can pack several image files and/or directories into one png file with minimum size.

Usage: java -jar SpriteMapper.jar [--options...] <image files or dirs...>

Config file:
  --config=CONFIG_FILE_NAME  - Use config file to pack many different sprites to differnt sprite maps.
                               Once config file is set, image files and dirs in argument is ignored.
                               See online help document for config file structure.
                               https://github.com/huandu/spritemapper/blob/master/README.md#config-file

Filter options:
  --include=[Ljava.lang.String;@7ecec0c5
                             - Only include files matching this pattern. Can set more than 1 filter.
  --exclude=PATTER           - Exclude files matching this pattern. Can set more than 1 filter.

Metadata options:
  --zwoptex2=meta{n}.plist   - Output metadata in Zwoptex2 general plist format.
  --keep-dir=false           - Keep dir name for frame keys in metadata file.

Output options:
  --output=spritemap{n}.png  - Output sprite map to 'spritemap{n}.png'. '{n}' is output sequence number.
                               Sequence number stars with 0 by default. Use '{n1}' to make the number
                               start with 1 instead of 0.
                               If --max-height is 0 or files can be packed in one sprite map, sequence
                               number will become an empty string. Use '{n!}' to force generating number.

Packing options:
  --base-dir=.               - Base dir of image files and directories.
  --algorithm=maxrects       - Set packing algorithm. Can be 'maxrects', 'guillotine' and/or 'shelf'.
                               Multiple algorithms can be used together, e.g. 'maxrects,guillotine,shelf'.
                               The most optimal algorithm will be chosen for final output.
  --max-width=1024           - Set maximum width. Default maximum width is 1024 pixels.
  --max-height=0             - Set maximum height. Default maximum height is 0, which means no limit.
                               If image files cannot be packed into one sprite due to max height,
  --use-pot-size=false       - Use POT (Power Of Two) value for width and height of sprite map.
  --draw-frames=false        - Draw frames around images in sprite map.
  --trim=false               - Trim transparent edges.
  --spacing=0                - Set sprite spacing.
  --border=0                 - Set border padding.

Others:
  --version                  - Show SpriteMapper version number.
  --help                     - Show this help message.

SpriteMapper is maintained by Huan Du <i@huandu.me>. Visit project page to get help or report bugs.
Project Page: https://github.com/huandu/spritemapper
```

Sprite Mapper will draw images in memory. It may consume hundreds MiB memory at runtime. Make sure to specify a large maximum memory size to avoid abnormal exits, e.g. `-Xmx2G`.

License
-------

SpriteMapper is licensed under GPLv3. See LICENSE for detail.

Dependency
----------

SpriteMapper uses [wildcard](https://github.com/EsotericSoftware/wildcard) library in binary form (the libs/wildcard.jar). Thank you, @EsotericSoftware.

History
-------

It was forked from [SpriteMapper](http://opensource.cego.dk/spritemapper/), which is developed by Robert Larsen. This fork is maintained by Huan Du.

Sprite Mapper 2 is **NOT** compatible with the original fork. If you are looking for old one, please check out [tag-v1](https://github.com/huandu/spritemapper/tree/tag-v1) instead.
