package io.github.notstirred.dasm.test.targets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@AllArgsConstructor
@Data
@Setter(AccessLevel.PRIVATE)
public class CubePos implements CubePosInterface {
    public static int MASK = 0;

    public int x;
    public int y;
    public int z;

    public CubePos(long cubePos) {
        this(extractX(cubePos), extractY(cubePos), extractZ(cubePos));
    }

    public static CubePos of(int x, int y, int z) {
        return new CubePos(x, y, z);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public long asLong() {
        long i = 0L;
        i |= ((long) this.x & (1 << 21) - 1) << 43;
        i |= ((long) this.y & (1 << 22) - 1);
        i |= ((long) this.z & (1 << 21) - 1) << 22;
        return i;
    }

    public static CubePos from(long cubePos) {
        return new CubePos(cubePos);
    }

    public static int extractX(long packed) {
        return (int) (packed >> 43);
    }

    public static int extractY(long packed) {
        return (int) (packed << 42 >> 42);
    }

    public static int extractZ(long packed) {
        return (int) (packed << 21 >> 43);
    }
}
