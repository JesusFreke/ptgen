/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jf.ptgen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.jf.ptgen.PTGen.RhombusOutput;
import org.jf.ptgen.penrose.BoundingBox;
import org.jf.ptgen.penrose.Rhombus;
import org.locationtech.jts.geom.Coordinate;

/**
 * This generates an SVG file with every rhombus represented as a separate path.
 *
 * This can be used to generate penrose tilings for display.
 */
class SvgOutput implements RhombusOutput {

    @Parameter(names={"--grid-spacing"}, description="How much space to leave between each " +
            "grid box.")
    protected double gridSpacing = 2.5;

    @Parameter(names={"--show-grid"}, description="If true, add a border around each grid " +
            "denoting the bounding box for that grid. Note that this is not a \"strict\" " +
            "bounding box, in that some rhombii on the edge will pass beyond it.")
    protected boolean showGrid = false;

    protected BoundingBox currentBox = null;

    @Override public void start(PTGen ptgen) {
        // How far can a single rhombus stick out past the bounding box containing it.
        // This is half of the long axis of a thin rhombus.
        double maxProtrusion = Math.sin(Math.toRadians(72));

        double xViewSize = ptgen.width * ptgen.countX + (ptgen.countX - 1) * gridSpacing
                + maxProtrusion * 2;
        double yViewSize = ptgen.height * ptgen.countY + (ptgen.countY - 1) * gridSpacing
                + maxProtrusion * 2;

        double xMinView = ptgen.minX - maxProtrusion;
        double yMinView = ptgen.minY - maxProtrusion;

        System.out.print("<svg width=\"" + xViewSize + "mm\"");
        System.out.print(" height=\"" + yViewSize + "mm\"");
        System.out.print(" viewBox=\"" + xMinView + " " + yMinView + " " + xViewSize
                + " " + yViewSize + "\"");
        System.out.println(">");
        generateStyle();
    }

    protected void generateStyle() {
        System.out.println("<style><![CDATA[");
        System.out.println("rect.boundingBox {");
        System.out.println("    stroke: blue;");
        System.out.println("    stroke-width: .05;");
        System.out.println("    fill-opacity: 0;");
        System.out.println("    stroke-opacity: .5;");
        System.out.println("}");
        System.out.println("path.thinRhombus {");
        System.out.println("    fill: #333333;");
        System.out.println("    stroke: #000000;");
        System.out.println("    stroke-width: .01;");
        System.out.println("}");
        System.out.println("path.thickRhombus {");
        System.out.println("    fill: #aaaaaa;");
        System.out.println("    stroke: #000000;");
        System.out.println("    stroke-width: .01;");
        System.out.println("}");
        System.out.println("]]></style>");
    }

    @Override public void startBox(BoundingBox boundingBox) {
        this.currentBox = boundingBox;
    }

    @Override public void endBox(BoundingBox boundingBox) {
        if (showGrid) {
            System.out.println(
                    String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" " +
                                    "class=\"boundingBox\"/>",
                            boundingBox.origin.x + boundingBox.xMultiple * gridSpacing,
                            boundingBox.origin.y + boundingBox.yMultiple * gridSpacing,
                            boundingBox.gridSize.getX(), boundingBox.gridSize.getY()));
        }
    }

    @Override public void visitRhombus(Rhombus rhombus) {
        assert(currentBox != null);

        System.out.print("<path");//);

        if (rhombus.getRhombusType() == Rhombus.THIN) {
            System.out.print(" class=\"thinRhombus\"");
        } else {
            System.out.print(" class=\"thickRhombus\"");
        }

        System.out.println(" id=\"rhombus_" + rhombus.strip1.stripFamily + "-" +
                rhombus.strip1.multiple + "_" + rhombus.strip2.stripFamily + "-" +
                rhombus.strip2.multiple + "\"");

        System.out.print(" d=\"M");

        for (Coordinate vertex: rhombus.getVertices()) {
            System.out.print(String.format(" %f,%f",
                    vertex.x + currentBox.xMultiple * gridSpacing,
                    vertex.y + currentBox.yMultiple * gridSpacing));
        }
        System.out.print(" z\">");

        System.out.println(String.format("<desc>%s, %s</desc></path>", rhombus.getLowerStrip(),
                rhombus.getUpperStrip()));
    }

    @Override public void end() {
        System.out.println("</svg>");
    }

    static void usage() {
        SvgOutput svgOutput = new SvgOutput();

        JCommander parser = JCommander.newBuilder()
                .addObject(svgOutput)
                .programName("--type=SVG")
                .build();

        parser.usage();
    }
}
