package curve;

import com.jogamp.opengl.GL2;
import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.vector.IVector;
import math.vector.Vector;
import model.DrawableObjectModel;

import java.awt.*;
import java.util.List;

public class ObjectOnCurve {

    private BSplineCurve curve;

    private DrawableObjectModel objectModel;

    private Color LINE_COLOR = Color.BLUE;

    private Color TANGENT_COLOR = Color.RED;

    private Color OBJECT_COLOR = Color.GREEN;

    private boolean DCM_OR_ROT = true;

    private IVector startOrientation = Vector.parseSimple("0 0 1");

    public ObjectOnCurve(List<IVector> points, DrawableObjectModel objectModel, int delta) {
        if (points.size() < 4) {
            throw new IllegalArgumentException("Must be given at least 4 points");
        }

        if(objectModel == null) {
            throw new IllegalArgumentException("Object model cannot be null");
        }

        this.curve = new BSplineCurve(points, delta);
        this.objectModel = objectModel.norming();
    }

    public boolean drawCurve(GL2 gl, boolean drawTangent, int tangentDelay, double tangentFactor, int objectStep) {
        BSplineCurve.bSplineIterator iterator = curve.iterator();
        boolean justObjected = false;

        IVector previousPoint = null;
        int step = 0;

        while(iterator.hasNext()) {
            IVector point = iterator.nextPoint();
            IVector tangent = iterator.nextTangent();
            justObjected = false;

            if(previousPoint != null) {
                gl.glBegin(GL2.GL_LINES);
                gl.glColor3f(LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue());
                gl.glVertex3d( point.get(0),  point.get(1),  point.get(2));
                gl.glVertex3d( previousPoint.get(0),  previousPoint.get(1),  previousPoint.get(2));
                gl.glEnd();



                if(drawTangent == true && step % tangentDelay == 0) {
                    drawTangent(gl, point, tangent, tangentFactor);
                }
                if(step == objectStep) {

                    drawObject(gl, point, tangent, iterator.secondDerivation());
                    justObjected = true;
                }

                step++;
            }
            previousPoint = point;
        }

        return justObjected;
    }


    private void drawTangent(GL2 gl, IVector point, IVector tangent, double tangentFactor) {
        IVector point2 = point.nAdd(tangent.nScalarMultiply(tangentFactor));

        gl.glBegin(GL2.GL_LINES);
        gl.glColor3f(TANGENT_COLOR.getRed(), TANGENT_COLOR.getGreen(), TANGENT_COLOR.getBlue());
        gl.glVertex3d(point.get(0), point.get(1), point.get(2));
        gl.glVertex3d(point2.get(0), point2.get(1), point2.get(2));
        gl.glEnd();

    }

    private void drawObject(GL2 gl, IVector point, IVector tangent, IVector secondDerivation) {
        gl.glPushMatrix();

        if(DCM_OR_ROT == true) {
            applyRotTransformation(gl, point, tangent);
        } else {
            IVector u = tangent.nVectorProduct(secondDerivation).normalize();
            IVector w = tangent;
            IVector v = w.nVectorProduct(u).normalize();

            applyDCMTransformation(gl, w, u, v, point);
        }

        gl.glColor3f(OBJECT_COLOR.getRed(), OBJECT_COLOR.getGreen(), OBJECT_COLOR.getBlue());
        objectModel.drawJogl(gl, true);

        gl.glPopMatrix();
    }

    private void applyRotTransformation(GL2 gl,IVector point, IVector tangent) {

        IVector rotationAxis = startOrientation.nVectorProduct(tangent);

        double cosAngle = startOrientation.cosine(tangent);
        double angle = Math.toDegrees(Math.acos(cosAngle));

        gl.glTranslated(point.get(0), point.get(1), point.get(2));
        gl.glRotated(angle, rotationAxis.get(0), rotationAxis.get(1), rotationAxis.get(2));

    }

    private void applyDCMTransformation(GL2 gl, IVector w, IVector u, IVector v, IVector point) {
        IMatrix invDMC = invDCM(w, u, v);

        double[] elements = new double[16];
        for(int row = 0; row < invDMC.getRowsCount(); row++) {
            for(int col = 0; col < invDMC.getColsCount(); col++) {
                elements[row * 4 + col] = invDMC.get(row, col);
            }
        }

        gl.glPushMatrix();

        gl.glTranslated(point.get(0), point.get(1), point.get(2));
        gl.glMultMatrixd(elements, 0);
    }

    public IMatrix invDCM(IVector w, IVector u, IVector v) {
        IMatrix dcm = new Matrix(4, 4);

        for(int row = 0; row < 3; row++) {
            dcm.set(row, 0, w.get(row));
            dcm.set(row, 1, u.get(row));
            dcm.set(row, 2, v.get(row));
        }

        dcm.set(3, 3, 1);

        return dcm.nInvert();
    }
}
