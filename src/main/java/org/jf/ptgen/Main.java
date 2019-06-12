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

import static org.jf.ptgen.OutputType.SVG;
import static org.jf.ptgen.OutputType.SVGLINE;

public class Main {

    @Parameter(names={"--minX", "-x"}, description="The minimum x value in pentagrid space of " +
            "the tiling to generate.")
    private Double minX = 0.0;

    @Parameter(names={"--minY", "-y"}, description="The minimum y value in pentagrid space of " +
            "the tiling to generate.")
    private Double minY = 0.0;

    @Parameter(names={"--width", "-w"}, description="The width of each grid square.")
    private Double width = 10.0;

    @Parameter(names={"--height", "-h"}, description="The height of each grid square.")
    private Double height = 10.0;

    @Parameter(names={"--countX", "-cx"}, description="The number of grids to generate, in the " +
            "x axis.")
    private Integer countX = 1;

    @Parameter(names={"--countY", "-cy"}, description="The number of grids to generate, in the " +
            "y axis.")
    private Integer countY = 1;

    @Parameter(names={"--type", "-t"}, converter = OutputType.Converter.class,
            description="Which type of output to generate.")
    private OutputType type = SVG;

    @Parameter(names={"--seed", "-s"}, description="The random seed used to generate the tiling.")
    private long seed = 0;

    @Parameter(names={"--help", "-?"}, help=true, description="Show this usage info.")
    private boolean help = false;

    public void doMain(RhombusOutput output) {
        PTGen ptGen = new PTGen(seed, minX, minY, width, height, countX, countY);
        
        ptGen.visitRhombii(output);
    }

    public static void main(String[] args) {
        Main main = new Main();
        RhombusOutput output = null;
        try {
            JCommander mainParser = JCommander.newBuilder()
                    .addObject(main)
                    .acceptUnknownOptions(true)
                    .build();

            mainParser.parse(args);

            if (main.help) {
                usage();
                return;
            }

            if (main.type == SVG) {
                output = new SvgOutput();
            } else if (main.type == SVGLINE) {
                output = new SvgLineOutput();
            }

            assert (output != null);

            JCommander outputParser = JCommander.newBuilder()
                    .addObject(output)
                    .build();

            outputParser.parse(mainParser.getUnknownOptions().toArray(new String[0]));

            if (outputParser.getUnknownOptions().size() > 0) {
                usage();
                return;
            }
        } catch (Exception ex) {
            usage();
            return;
        }

        main.doMain(output);
    }

    private static void usage() {
        Main main = new Main();

        JCommander mainParser = JCommander.newBuilder()
                .addObject(main)
                .programName("ptgen")
                .build();
        mainParser.usage();

        SvgOutput.usage();

        SvgLineOutput.usage();
    }
}
