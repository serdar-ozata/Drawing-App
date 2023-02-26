package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class Drawing implements Initializable {
    public Pane root;
    boolean isCornerMoved;
    double circleX;
    double circleY;
    int theCornerInUse;
    int dataSeq;
    public MenuItem redoButton;
    public MenuItem undoButton;
    public MenuItem cornerDeleteButton;
    public ColorPicker colorPicker;
    ArrayList<ArrayList<ArrayList<Double>>> rootData = new ArrayList<>();
    ArrayList<Circle> corners;
    Polygon poly;
    Color preferredColor;

    EventHandler<MouseEvent> draggingInitialize = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            Circle srcCircle = (Circle) e.getSource();
            for (int i = 0; i < corners.size(); i++) {
                if (srcCircle == corners.get(i)) {
                    theCornerInUse = i;
                    break;
                }
            }
            corners.get(theCornerInUse).setFill(Color.RED);
            isCornerMoved = false;
        }
    };
    EventHandler<MouseEvent> circleDragging = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            circleX = e.getX();
            circleY = e.getY();
            Circle corner = corners.get(theCornerInUse);
            if (Math.abs(corner.getCenterX() - circleX) > 1 || Math.abs(corner.getCenterY() - circleY) > 1)
                isCornerMoved = true;
            deactivateCornerOpts();
            moveCornerAndCircle(corner);
        }
    };

    EventHandler<MouseEvent> finishDragging = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if (isCornerMoved) {
                corners.get(theCornerInUse).setFill(preferredColor.darker());
                addCurrentData();
                fixRedoUndoOptions();
                System.out.println(dataSeq + " Circle drag finish");
            } else {
                activateCornerOpts();
            }

        }
    };

    EventHandler<MouseEvent> rectangleDrawInit = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            double x1 = event.getX();
            double y1 = event.getY();
            poly = new Polygon();
            for (int i = 0; i < 8; i += 2) {
                poly.getPoints().add(x1);
                poly.getPoints().add(y1);
            }
            poly.setFill(Color.rgb(0, 0, 10, 0));
            poly.setStrokeWidth(3);
            poly.setStroke(Color.LIGHTGRAY);
            root.getChildren().add(poly);

        }
    };

    EventHandler<MouseEvent> rectangleDraw = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            double x2 = e.getX();
            double y2 = e.getY();
            poly.getPoints().set(2, x2);
            poly.getPoints().set(4, x2);
            poly.getPoints().set(5, y2);
            poly.getPoints().set(7, y2);

        }
    };
    EventHandler<MouseEvent> rectangleDrawFinish = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            poly.setStroke(preferredColor.darker());
            root.removeEventFilter(MouseEvent.MOUSE_PRESSED, rectangleDrawInit);
            root.removeEventFilter(MouseEvent.MOUSE_DRAGGED, rectangleDraw);
            root.removeEventFilter(MouseEvent.MOUSE_RELEASED, rectangleDrawFinish);
            corners = createCircles(0, poly.getPoints().size());
            root.addEventFilter(MouseEvent.MOUSE_PRESSED, cornerInitialize);
            rootData.add(getCurrentData());
            dataSeq = 0;
            System.out.println(dataSeq + " rect draw finish");
        }

    };

    EventHandler<MouseEvent> cornerInitialize = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            double x = e.getX();
            double y = e.getY();
            ArrayList<ArrayList<Double>> possLocations = new ArrayList<>();
            ArrayList<Double> rightLocation;
            double cornerX1 = corners.get(corners.size() - 1).getCenterX();
            double cornerY1 = corners.get(corners.size() - 1).getCenterY();
            double cornerX2;
            double cornerY2;
            double firstCornerDist;
            double secondCornerDist;
            double distBetweenCorners;
            double distance;
            double firstCornerSeqD;
            for (int i = 0; i < corners.size(); i++) {
                cornerX2 = corners.get(i).getCenterX();
                cornerY2 = corners.get(i).getCenterY();
                firstCornerDist = Math.hypot(x - cornerX1, y - cornerY1);
                secondCornerDist = Math.hypot(x - cornerX2, y - cornerY2);
                distBetweenCorners = Math.hypot(cornerX1 - cornerX2, cornerY1 - cornerY2);
                if (firstCornerDist < 5 || secondCornerDist < 5)
                    return;
                distance = firstCornerDist + secondCornerDist - distBetweenCorners;
                if (distance < 1) {
                    ArrayList<Double> list = new ArrayList<>();
                    list.add((double) i);
                    list.add(distance);
                    possLocations.add(list);
                }

                cornerX1 = cornerX2;
                cornerY1 = cornerY2;
            }
            if (possLocations.size() > 0) {
                fixRedoUndoOptions();
                rightLocation = possLocations.get(0);
                for (ArrayList<Double> loc : possLocations) {
                    if (rightLocation.get(1) > loc.get(1))
                        rightLocation = loc;
                }
                firstCornerSeqD = rightLocation.get(0);
                theCornerInUse = (int) firstCornerSeqD;
                poly.getPoints().add(2 * theCornerInUse, y);
                poly.getPoints().add(2 * theCornerInUse, x);
                corners.add(theCornerInUse, createCircles(2 * theCornerInUse, 2 * theCornerInUse + 1).get(0));
                corners.get(theCornerInUse).setFill(Color.RED);
                root.addEventFilter(MouseEvent.MOUSE_DRAGGED, circleDragging);
                root.addEventFilter(MouseEvent.MOUSE_RELEASED, cornerFinish);
            }

        }
    };

    EventHandler<MouseEvent> cornerFinish = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            root.removeEventFilter(MouseEvent.MOUSE_DRAGGED, circleDragging);
            root.removeEventFilter(MouseEvent.MOUSE_RELEASED, cornerFinish);
            corners.get(theCornerInUse).setFill(preferredColor.darker());
            rootData.add(getCurrentData());
            dataSeq++;
            System.out.println(dataSeq + " Corner finish");
        }
    };


    private ArrayList<Circle> createCircles(int startPt, int endPt) {
        ArrayList<Circle> circleList = new ArrayList<>();
        for (int i = startPt; i < endPt; i += 2) {
            Circle circle = new Circle();
            circle.setCenterX(poly.getPoints().get(i));
            circle.setCenterY(poly.getPoints().get(i + 1));
            circle.setRadius(4);
            circle.setFill(preferredColor.darker());
            circleList.add(circle);
        }
        circleList.forEach((n) -> {
            root.getChildren().add(n);
            n.addEventFilter(MouseEvent.MOUSE_PRESSED, draggingInitialize);
            n.addEventFilter(MouseEvent.MOUSE_DRAGGED, circleDragging);
            n.addEventFilter(MouseEvent.MOUSE_RELEASED, finishDragging);
            n.addEventFilter(MouseEvent.MOUSE_CLICKED, clickCorner);
        });
        return circleList;
    }

    private void moveCornerAndCircle(Circle circle) {
        circle.setCenterX(circleX);
        circle.setCenterY(circleY);
        poly.getPoints().set(2 * theCornerInUse, circleX);
        poly.getPoints().set(2 * theCornerInUse + 1, circleY);
    }

    private ArrayList<ArrayList<Double>> getCurrentData() {
        ArrayList<ArrayList<Double>> data = new ArrayList<>();
        for (Circle circle : corners) {
            ArrayList<Double> aCorner = new ArrayList<>();
            aCorner.add(circle.getCenterX());
            aCorner.add(circle.getCenterY());
            data.add(aCorner);
        }
        return data;
    }


    private void addCurrentData() {
        rootData.add(getCurrentData());
        dataSeq++;
    }

    private void alterData(int newDataSeq) {
        for (Circle corner : corners) {
            root.getChildren().remove(corner);

        }
        ArrayList<ArrayList<Double>> newData = rootData.get(dataSeq + newDataSeq);
        for (int i = 0; i < rootData.get(dataSeq).size(); i++) {
            poly.getPoints().remove(0);
            poly.getPoints().remove(0);
        }
        for (ArrayList<Double> newDatum : newData) {
            poly.getPoints().add(newDatum.get(0));
            poly.getPoints().add(newDatum.get(1));
        }
        corners = createCircles(0, poly.getPoints().size());
    }

    EventHandler<MouseEvent> clickCorner = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            System.out.println("click");
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        preferredColor = Color.BLACK;
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, rectangleDrawInit);
        root.addEventFilter(MouseEvent.MOUSE_DRAGGED, rectangleDraw);
        root.addEventFilter(MouseEvent.MOUSE_RELEASED, rectangleDrawFinish);
        redoButton.setDisable(true);
        undoButton.setDisable(true);
    }

    public void undoDraw(ActionEvent actionEvent) {
        redoButton.setDisable(false);
        alterData(-1);
        dataSeq--;
        if (dataSeq == 0)
            undoButton.setDisable(true);
    }

    public void redoDraw(ActionEvent actionEvent) {
        alterData(1);
        dataSeq++;
        undoButton.setDisable(false);
        if (dataSeq + 1 == rootData.size())
            redoButton.setDisable(true);
    }

    private void activateCornerOpts() {
        cornerDeleteButton.setDisable(false);
    }

    private void deactivateCornerOpts() {
        cornerDeleteButton.setDisable(true);
    }

    public void deleteCorner(ActionEvent actionEvent) {
        deactivateCornerOpts();
        Circle circle = corners.get(theCornerInUse);
        root.getChildren().remove(circle);
        poly.getPoints().remove(2 * theCornerInUse);
        poly.getPoints().remove(2 * theCornerInUse);
        corners.remove(theCornerInUse);
        addCurrentData();
        fixRedoUndoOptions();
        System.out.println("Corner delete " + dataSeq);
    }

    private void fixRedoUndoOptions() {
        redoButton.setDisable(true);
        undoButton.setDisable(false);
        int lim = rootData.size();
        if (lim > dataSeq + 1) {
            rootData.subList(dataSeq + 1, lim).clear();
        }
    }
    @FXML
    public void changeColor() {
        preferredColor = colorPicker.getValue();
        poly.setFill(preferredColor);
        poly.setStroke(preferredColor.darker());
        corners.forEach((n) -> n.setFill(preferredColor.darker()));
    }

}
