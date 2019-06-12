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
 * Represents the angle of one of the five line families.
 */
public class PentAngle {

    private final int angleIndex;
    private final int degrees;
    private final double radians;

    public final double sin;
    public final double cos;

    public static PentAngle[] PENTANGLES = new PentAngle[] {
            new PentAngle(0), // 0, 1
            new PentAngle(1), //
            new PentAngle(2),
            new PentAngle(3),
            new PentAngle(4)
    };

    private PentAngle(int angleIndex) {
        this.angleIndex = angleIndex;

        this.degrees = 72 * angleIndex;
        this.radians = Math.PI * 2 * degrees / 360;

        this.sin = PenroseTiling.PRECISION_MODEL.makePrecise(Math.sin(radians));
        this.cos = PenroseTiling.PRECISION_MODEL.makePrecise(Math.cos(radians));
    }

    /**
     * @return A unit vector in the direction of this PentAngle.
     */
    public Vector2D unit() {
        return new Vector2D(sin, cos);
    }

    /**
     * Get the sin of the angle between this pentangle and another pentangle
     */
    public double sin(PentAngle other) {
        return Math.sin(other.radians - radians);
    }
}
