package io.github.notstirred.dasm.test.targets;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChunkPos {
    public int x;
    public int z;

    public static ChunkPos fromLong(long l) {
        return null;
    }
}
