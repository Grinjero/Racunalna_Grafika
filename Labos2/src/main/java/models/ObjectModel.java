package models;

import math.vector.IVector;
import math.vector.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ObjectModel {

    public List<Vertex3D> points;

    public List<Face3D> faces;


    private ObjectModel() {
        points = new ArrayList<Vertex3D>();
        faces = new ArrayList<Face3D>();
    }

    public ObjectModel(String filename) throws IOException {
        parse(filename);
    }

    public ObjectModel(List<String> input) {
        this();

        parse(input);
    }

    public ObjectModel(List<Vertex3D> points, List<Face3D> faces) {
        this.points = points;
        this.faces = faces;
    }



    public ObjectModel copy() {
        List<Vertex3D> newPoints = new ArrayList<Vertex3D>(points);
        List<Face3D> newFaces = new ArrayList<Face3D>(faces);

        return new ObjectModel(newPoints, newFaces);
    }

    private void parse(String filename) throws IOException {
        String pathString = "src/main/resources/models/" + filename;
        Path path = Paths.get(filename);

        if(Files.notExists(path)) {
            throw new IllegalArgumentException("Given file does not exist within the models directory");
        }

        List<String> lines = Files.readAllLines(path);
        parse(lines);
    }

    private void parse(List<String> input) {
        for(String line : input) {

            String[] splits = line.split(" ");

            if(splits.length != 4) {
                continue;
            }

            if(splits[0].equals("v")) {
                try {
                    double x = Double.parseDouble(splits[1]);
                    double y = Double.parseDouble(splits[2]);
                    double z = Double.parseDouble(splits[3]);

                    points.add(new Vertex3D(x, y, z));

                } catch(NumberFormatException exc) {
                    System.out.println("Could not parse point: \"" + line + "\"" );
                    continue;
                }

            } else if(splits[0].equals("f")) {
                Face3D face = new Face3D(splits.length - 1);
                try {
                    for(int i = 1; i < splits.length; i++) {
                        int index  = Integer.parseInt(splits[i]);

                        face.addNextIndex(index, points);
                    }

                } catch(NumberFormatException exc) {
                    System.out.println("Could not parse polygon: \"" + line + "\"");
                    continue;
                }

                faces.add(face);
            }
        }
    }

    public ObjectModel norming() {
        ObjectModel normed = this.copy();

        IVector firstPoint = points.get(0).getVector();

        double[] mins = new double[] { firstPoint.get(0), firstPoint.get(1), firstPoint.get(2) };
        double[] maxes = new double[3];

        for(Vertex3D point : points) {
            IVector vector = point.getVector();

            for(int i = 0; i < mins.length; i++) {
                if(vector.get(i) < mins[i]) {
                    mins[i] = vector.get(i);
                }

                if(vector.get(i) > maxes[i]) {
                    maxes[i] = vector.get(i);
                }
            }
        }

        double[] averages = new double[3];
        double maxRange = 0;



        for(int i = 0; i < averages.length; i++) {
            averages[i] = (mins[i] + maxes[i]) / 2;

            double difference = maxes[i] - mins[i];
            if(difference > maxRange) {
                maxRange = difference;
            }
        }

        Vector averageVector = new Vector(averages);

        double modifier = 2 / maxRange;
        for(Vertex3D vertex : normed.points) {
            vertex.getVector().sub(averageVector).scalarMultiply(modifier);
        }

        return normed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(Vertex3D point : points) {
            sb.append(point.toString() + "\n");
        }

        for(Face3D face : faces) {
            sb.append(face.toString() + "\n");
        }

        return sb.toString();
    }
}


