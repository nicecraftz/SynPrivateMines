/**
 * MIT License
 * <p>
 * Copyright (c) 2021 - 2023 Kyle Hicks
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.untouchedodin0.privatemines.utils;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.List;

public class ExpansionUtils {

    public static final List<BlockVector3> EXPANSION_VECTORS = List.of(
            BlockVector3.UNIT_X,
            BlockVector3.UNIT_MINUS_X,
            BlockVector3.UNIT_Z,
            BlockVector3.UNIT_MINUS_Z
    );

    public static BlockVector3[] expansionVectors(final int amount) {
        return EXPANSION_VECTORS.stream().map(it -> it.multiply(amount)).toArray(BlockVector3[]::new);
    }

    public static BlockVector3[] contractVectors(final int amount) {
        return EXPANSION_VECTORS.stream().map(it -> it.divide(amount)).toArray(BlockVector3[]::new);
    }
}
