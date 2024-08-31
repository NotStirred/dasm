package io.github.notstirred.dasm.test.targets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter(AccessLevel.PRIVATE)
@Data
public class Vec3i {
    public int x;
    public int y;
    public int z;

    public Vec3i(long packed) {
        this(extractX(packed), extractY(packed), extractZ(packed));
    }

    public static Vec3i fromLong(long l) {
        return new Vec3i(l);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
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

    public static Vec3i from(long cubePos) {
        return new Vec3i(cubePos);
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
