package math.util;

import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.vector.IVector;
import math.vector.Vector;

import java.util.ArrayList;
import java.util.List;

public class IRG {

    public static IMatrix createIdentityMatrix(int size) {
        Matrix matrix = new Matrix(size, size);

        for(int i = 0; i < size; i++) {
            matrix.set(i, i, 1);
        }

        return matrix;
    }

    public static IMatrix translate3D(float dx, float dy, float dz) {
        IMatrix matrix = createIdentityMatrix(4);

        matrix.set(3, 0, dx);
        matrix.set(3, 1, dy);
        matrix.set(3, 2, dz);

        return matrix;
    }

    public static IMatrix scale3D(float sx, float sy, float sz) {
        IMatrix matrix = createIdentityMatrix(4);

        matrix.set(0,0, sx);
        matrix.set(1, 1, sy);
        matrix.set(2, 2, sz);

        return matrix;
    }
}
