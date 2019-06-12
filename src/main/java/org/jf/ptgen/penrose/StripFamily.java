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

import org.locationtech.jts.math.Vector2D;

/**
 * Represents 1 of the 5 families of strips of rhombii in de Bruijn's method.
 */
public class StripFamily {
    public final PenroseTiling tiling;
    public final double offset;
    public final int angle;

    public StripFamily(PenroseTiling tiling, double offset, int angle) {
        this.tiling = tiling;
        this.offset = offset;
        this.angle = angle;
    }

    public Strip getStrip(int multiple) {
        return new Strip(this, multiple);
    }

    public Vector2D getDirection() {
        return PentAngle.PENTANGLES[angle].unit();
    }

    public Vector2D getOffsetDirection() {
        // TODO: make sure this is offset in the correct direction
        return getDirection().rotateByQuarterCircle(-1);
    }

    public double angle() {
        return (Math.PI * 2 / 5) * angle;
    }

    public PentAngle pentangle() {
        return PentAngle.PENTANGLES[angle];
    }
}
