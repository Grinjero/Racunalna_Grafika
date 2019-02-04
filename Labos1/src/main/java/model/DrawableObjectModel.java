package model;

import com.jogamp.opengl.GL2;
import math.matrix.IMatrix;
import math.util.IRG;
import math.vector.IVector;
import math.vector.Vector;

import java.util.ArrayList;
import java.util.List;

public class DrawableObjectModel extends ObjectModel {

    private boolean drawPolygons;

    private boolean joglOrManual;

    private Rejection rejection = Rejection.REJECTION_3;


    public DrawableObjectModel(List<String> input, boolean drawPolygons) {
        super(input);

        this.drawPolygons = drawPolygons;
    }

    public DrawableObjectModel(List<Vertex3D> points, List<Face3D> faces, boolean drawPolygons) {
        super(points, faces);

        this.drawPolygons = drawPolygons;
    }

    public DrawableObjectModel(ObjectModel objectModel, boolean drawPolygons, boolean joglOrManual) {
        super(objectModel.points, objectModel.faces);

        this.drawPolygons = drawPolygons;
        this.joglOrManual = joglOrManual;
    }

    public void drawWire(GL2 gl, IMatrix transformation, IVector eye) {
        if(joglOrManual == false) {
            determineVisibility(eye, transformation);
        }

        for(Face3D face : super.faces) {
            if(face.isVisible() == false) {
                continue;
            }

            if(drawPolygons == false) {
                gl.glBegin(GL2.GL_LINE_LOOP);

            } else {
                gl.glBegin(GL2.GL_POLYGON);
            }

            gl.glColor3f(1F, 0.0F, 0.0F);

            for(int i = 0; i < face.size(); i++) {
                Vertex3D point = points.get(face.getElementAt(i));


                if(transformation == null) {
                    gl.glVertex3f((int) point.getElementAt(0), (int) point.getElementAt(1), (int) point.getElementAt(2));
                } else {

                    IVector vector = new Vector(new double[]{point.getElementAt(0), point.getElementAt(1), point.getElementAt(2), 1});
                    vector = vector.toRowMatrix(false).nMultiply(transformation).toVector(false);
                    vector = vector.nFromHomogeneous();

                    gl.glVertex3f((float) vector.get(0), (float) vector.get(1), (float) vector.get(2));
                }
            }

            gl.glEnd();
        }
    }

    public void determineFaceVisibilities1(IVector eye) {
        eye = eye.copyPart(4);
        eye.set(3, 1);

        for(Face3D face : faces) {
            if(eye.scalarProduct(face.getCoefficients()) > 0) {
                face.setVisibility(true);

            } else {
                face.setVisibility(false);
            }
        }
    }

    public void determineFaceVisibilities2(IVector eye) {
        for(Face3D face : faces) {
            if(face.polygonCenter == null) {
                face.polygonCenter = new Vector(3, false);

                for(int i = 0; i < face.size(); i++) {
                    face.polygonCenter.add(points.get(face.getElementAt(i)).getVector());
                }

                face.polygonCenter.scalarMultiply((double) 1/3);
            }

            IVector forward = eye.nSub(face.polygonCenter);
            forward = forward.copyPart(forward.getDimension() + 1);
            forward.set(forward.getDimension() - 1, 1);

            if(forward.cosine(face.getCoefficients()) > 0) {
                face.setVisibility(true);
            } else {
                face.setVisibility(false);
            }
        }
    }

    public void determineFaceVisibilities3(IMatrix transformation) {
        for(Face3D face : faces) {
            List<IVector> vertexVectors = face.getVertexVectors(points);

            face.setVisibility(IRG.isAntiClockwise(vertexVectors, transformation));
        }
    }

    public void setAllToVisible() {
        for(Face3D face : faces) {
            face.setVisibility(true);
        }
    }

    public void determineVisibility(IVector eye, IMatrix transformation) {
        switch (rejection) {
            case NO_REJECTION:
                setAllToVisible();
                break;

            case REJECTION_1:
                determineFaceVisibilities1(eye);
                break;

            case REJECTION_2:
                determineFaceVisibilities2(eye);
                break;

            case REJECTION_3:
                determineFaceVisibilities3(transformation);
                break;
        }
    }



    public void setRejection(Rejection rejection) {
        this.rejection = rejection;
    }

    public void drawJogl(GL2 gl, boolean gouradOrConst) {
        if(gouradOrConst == true) {
            for(Face3D face : faces) {
                IVector normal = face.getCoefficients().normalize();

                gl.glBegin(GL2.GL_POLYGON);

                for(Vertex3D vertex : face.getVertices(points)) {
                    IVector vector = vertex.getVector();

                    gl.glNormal3f((float) normal.get(0), (float) normal.get(1), (float) normal.get(2));
                    gl.glVertex3f((float) vector.get(0), (float) vector.get(1), (float) vector.get(2));
                }

                gl.glEnd();
            }
        } else {
            calculateNormals();
            for (Face3D face : faces) {
                gl.glBegin(GL2.GL_POLYGON);

                for (Vertex3D vertex : face.getVertices(points)) {
                    IVector vector = vertex.getVector();
                    IVector normal = vertex.getNormalVector();

                    gl.glNormal3f((float) normal.get(0), (float) normal.get(1), (float) normal.get(2));
                    gl.glVertex3f((float) vector.get(0), (float) vector.get(1), (float) vector.get(2));
                }

                gl.glEnd();
            }
        }
    }

    public void calculateNormals() {
        for(int i = 0; i < points.size(); i++) {
            if(points.get(i).isNormalVectorSet() == true) {
                break;
            }

            List<IVector> faceNormals = new ArrayList<IVector>();

            for(Face3D face : faces) {
                if(face.containsIndex(i + 1)) {
                    faceNormals.add(face.getCoefficients().normalize());
                }
            }

            points.get(i).setNormalVector(IRG.arithmeticMiddleOfVectors(faceNormals).normalize());
        }
    }

    public DrawableObjectModel norming() {
        return new DrawableObjectModel(super.norming(), true, true);
    }
}
