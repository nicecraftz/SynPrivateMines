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

package me.untouchedodin0.privatemines.mine;

import me.untouchedodin0.kotlin.mine.type.MineType;
import me.untouchedodin0.privatemines.PrivateMines;

import java.util.HashMap;
import java.util.Map;

public class MineTypeRegistry {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private final HashMap<String, MineType> mineRegistry = new HashMap<>();
    private Mine defaultMine = null;

    public void register(MineType mineType) {
        mineRegistry.put(mineType.getName().toLowerCase(), mineType);
    }

    public MineType get(String name) {
        return mineRegistry.get(name.toLowerCase());
    }

    public boolean isDefaultMineSet() {
        return defaultMine != null;
    }

    public Mine getDefaultMine() {
        return defaultMine;
    }

    public MineType getNextMineType(MineType current) {
        for (Map.Entry<String, MineType> entry : mineRegistry.entrySet()) {
            if (entry.getValue().equals(current)) {
                return mineRegistry.entrySet()
                        .stream()
                        .skip(mineRegistry.entrySet().stream().toList().indexOf(entry) + 1)
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse(current);
            }
        }
        return null;
    }

    public Map<String, MineType> getMineRegistry() {
        return Map.copyOf(mineRegistry);
    }
}
