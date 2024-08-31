package io.github.notstirred.dasm.test.targets;

public interface CubePosInterface {
    int x();

    int y();

    int z();

    void setX(int x);

    void setY(int y);

    void setZ(int z);

    static CubePosInterface createCubePos(int x, int y, int z) {
        return new CubePos(x, y, z);
    }
}
