Sprite Mapper
=============

SpriteMapper can pack several image files and/or directories into one png file with minimum size. It can write Zwoptex compatible metadata file for sprite map.

It's forked from http://opensource.cego.dk/spritemapper/. Original author is Robert Larsen. This fork is maintained by Huan Du.

Quick Guide
-----------

Use Sprite Mapper in following 3 steps.

Step 1: Download source code from GitHub.

`wget https://github.com/huandu/spritemapper/archive/master.zip`

Step 2: Unzip source code and run `ant` in code dir to build it.

```
[huandu@localhost Downloads]$ unzip spritemapper-master.zip
[huandu@localhost Downloads]$ cd spritemapper-master
[huandu@localhost spritemapper-master]$ ant
```

Compiled jar file is located in `./dist/SpriteMapper.jar`.

Step 3: Make your first sprite map with Sprite Mapper. 

```
java -jar dist/SpriteMapper.jar \
	--cocos2d=my_first.plist --out=my_first.png \
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

Usage: java -jar SpriteMapper.jar [options...] <image files or dirs...>

Input options:
    --base-dir=.               - Base dir of image files and directories.
    --file-pattern=\.((png)|(jpe?g)|(gif))$
                               - Only include files matching this pattern.

Metadata options:
    --zwoptex2=zwoptex2.plist  - Output metadata in Zwoptex2 general plist format.
    --cocos2d=cocos2d.plist    - Output metadata in cocos2d plist format.
    --reserve-dir-name=false   - Reserve dir name for frame keys in metadata file.

Packing options:
    --out=spritemap.png        - Output sprite map to 'spritemap.png'.
    --algorithm=maxrects       - Set packing algorithm. Can be 'maxrects', 'guillotine' and/or 'shelf'.
                                 Multiple algorithms can be used together, e.g. 'maxrects,guillotine,shelf'.
                                 The most optimal algorithm will be chosen for final output.
    --max-width=1024           - Set maximum width of sprite map to 1024 pixels.
    --pot-size=false           - Use POT (Power Of Two) value for width and height of sprite map.
    --draw-frames=false        - Draw frames around images in sprite map.
    --trim=true                - Trim transparent edges.
    --spacing=0                - Set sprite spacing.
    --border=0                 - Set border padding.

Others:
    --help                     - Show this help message.
```

Sprite Mapper will draw images in memory. It may consume hundreds MiB memory at runtime. Make sure to specify a large maximum memory size to avoid abnormal exits, e.g. `-Xmx2G`.

**Integrate with `ant`**

This is the most well maintained way to run the mapper as this is what I do myself. Get Ant from here.

*Note*: Only a part of features are supported in `ant` task. Related code will be updated to support full command line features later.

Somewhere in your build file you must load the spritemapper `ant` tasks:

```
<taskdef resource="spritemapper-tasks"/>
```

The following target shows all supported attributes:

```
<target name="spritemap">
    <taskdef resource="spritemapper-tasks"/>
    <spritemap trim="true" drawframes="false" maxwidth="800" destfile="spritemap.png">
        <layoutmeta destfile="spritemap.plist" format="zwoptex2"/>
        <fileset dir="graphics">
            <include name="**/*.png"/>
        </fileset>
    </spritemap>
</target>
```

Each tag/parameter will be described in the following.

`<spritemap>`

The spritemap tag is the task for building the spritemap. The images that should be used as input are specified as a nested fileset tag. spritemap supports the following attributes:

* `trim` - Optional. Remove transparent edges. The meta output formats specifies how to recreate the original image. Defaults to true.
* `drawframes` - Optional. Draw a red frame around each image. This is for debugging only. Defaults to false.
* `maxwidth` - Required. Maximum width in pixels of the resulting sprite map.
destfile - Required. File to write resulting sprite map to.

`<layoutmeta>`

This tag defines a file that should contain the coordinate information for the sprite map. It is contained within the spritemap tag. Multiple tags are allowed. It supports the following attributes:

* `destfile` - Required. File to write content to.
* `format` - Optional. Format of output. Defaults to 'zwoptex2' which currently is the only supported format.

License
-------

This software is licensed under GPLv3. See LICENSE for detail.