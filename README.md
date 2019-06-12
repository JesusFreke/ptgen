##### ptGen - A P3 Penrose Tiling Generator

This is a little program to generate P3 penrose tilings as SVGs. It
supports 2 types of outputs.

The first is a colored SVG output where each rhombus is specified as a
separate path, and with different styles applied to thick and thin
rhombii. This is intended to be useful to generate tilings for display.

The second is a more minimal SVG output that only includes the edges
around each rhombus. The edges are de-duplicated, so that there is only
a single line segment for an edge shared by 2 rhombii. This is intended
to be useful for milling/engraving applications, to avoid re-cutting
the shared edges between rhombii.

This program has the ability to split up a larger tiling
into multiple smaller tilings in a grid, that, when rejoined, have no
overlaps or gaps.

##### Getting started
1. Download the ptgen.jar
2. `java -jar ptgen.jar > tiling.svg` to generate a basic 10mm x 10mm tiling
3. `java -jar ptgen-jar --help` to see what other options are available.

##### Generation algorithm
This uses de Bruijn's method to generate the tiling. You can read more
[here](https://www.mathpages.com/home/kmath621/kmath621.htm) and
[here](http://www.ams.org/publicoutreach/feature-column/fcarc-ribbons)

##### Custom Output
If you need output in some other format, it should be relatively easy
to implement a new output format by implementing the
PTGen.RhombusOutput interface.

--------

Note: This is not an officially supported Google product.
