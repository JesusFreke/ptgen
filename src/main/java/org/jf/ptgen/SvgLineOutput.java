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
import org.jf.ptgen.penrose.BoundingBox;
import org.jf.ptgen.penrose.Rhombus;
import org.locationtech.jts.geom.Coordinate;

import java.util.HashSet;
import java.util.Set;

/**
 * This generates an SVG file that only contains the rhombus edges as lines.
 *
 * The shared edges between 2 rhombii are deduplicated, which is useful when engraving on a CNC
 * mill, etc. to avoid re-engraving/cutting/whatever the same line twice.
 */
public class SvgLineOutput extends SvgOutput {

    private class Edge {
        public Coordinate first;
        public Coordinate second;

        public Edge(Coordinate first, Coordinate second) {
            this.first = first;
            this.second = second;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Edge edge = (Edge)o;

            if (!first.equals(edge.first)) return false;
            return second.equals(edge.second);
        }

        @Override public int hashCode() {
            int result = first.hashCode();
            result = 31 * result + second.hashCode();
            return result;
        }

        public Edge reversed() {
            return new Edge(second, first);
        }
    }

    private Set<Edge> currentBoxEdges = new HashSet<>();

    @Override protected void generateStyle() {
        System.out.println("<style><![CDATA[");
        System.out.println("rect.boundingBox {");
        System.out.println("    stroke: blue;");
        System.out.println("    stroke-width: .05;");
        System.out.println("    fill-opacity: 0;");
        System.out.println("    stroke-opacity: .5;");
        System.out.println("}");
        System.out.println("path.rhombusEdge {");
        System.out.println("    stroke: #000000;");
        System.out.println("    stroke-width: .01;");
        System.out.println("}");
        System.out.println("]]></style>");
    }

    @Override public void startBox(BoundingBox boundingBox) {
        super.startBox(boundingBox);
        currentBoxEdges.clear();
    }

    @Override public void visitRhombus(Rhombus rhombus) {
        Coordinate[] vertices = rhombus.getVertices();
        Coordinate previousVertex = vertices[vertices.length - 1];
        for (Coordinate vertex: vertices) {
            Edge edge = new Edge(previousVertex, vertex);

            if (currentBoxEdges.contains(edge) || currentBoxEdges.contains(edge.reversed())) {
                previousVertex = vertex;
                continue;
            }

            System.out.print("<path class=\"rhombusEdge\"");
            System.out.print(" id=\"edge" + currentBoxEdges.size() + "\"");

            System.out.print(" d=\"M");
            System.out.print(String.format(" %f,%f",
                    previousVertex.x + currentBox.xMultiple * gridSpacing,
                    previousVertex.y + currentBox.yMultiple * gridSpacing));
            System.out.print(String.format(" %f,%f",
                    vertex.x + currentBox.xMultiple * gridSpacing,
                    vertex.y + currentBox.yMultiple * gridSpacing));
            System.out.println("\"/>");

            currentBoxEdges.add(edge);
            previousVertex = vertex;
        }
    }

    static void usage() {
        SvgLineOutput svgLineOutput = new SvgLineOutput();

        JCommander parser = JCommander.newBuilder()
                .addObject(svgLineOutput)
                .programName("--type=SVGLINE")
                .build();

        parser.usage();
    }
}
