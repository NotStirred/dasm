package io.github.notstirred.dasm.test.targets;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CubePos {
    public int x;
    public int y;
    public int z;

    public static CubePos fromLong(long l) {
        return null;
    }
}
