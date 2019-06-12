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

package org.jf.ptgen.penrose;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class PenroseTilingTest {
    @Test
    public void testStripIntersection() {
        Random random = new Random(0);
        PenroseTiling tiling = new PenroseTiling(random);

        StripFamily initialFamily = tiling.getStripFamily(0);
        Strip initialStrip = initialFamily.getStrip(4);
        Rhombus initialRhombus = initialStrip.getRhombus(4);

        Assert.assertEquals(initialStrip.getRhombii(initialRhombus.strip2, true).iterator().next(), initialRhombus);
    }
}
