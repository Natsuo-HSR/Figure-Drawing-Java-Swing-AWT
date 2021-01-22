import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

public class ShapeTest
{
    public static void main(String[] args)
    {
        JFrame frame = new ShapeTestFrame();
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
    }
}

class ShapeTestFrame extends JFrame
{
    public ShapeTestFrame()
    {
        setTitle("Shape Test");
        setSize(500, 500);

        final ShapePanel panel = new ShapePanel();
        add(panel, BorderLayout.CENTER);
        final JComboBox comboBox = new JComboBox();
        comboBox.addItem(new LineMaker());
        comboBox.addItem(new RectangleMaker());
        comboBox.addItem(new RoundRectangleMaker());
        comboBox.addItem(new EllipseMaker());
        comboBox.addItem(new ArcMaker());
        comboBox.addItem(new PolygonMaker());
        comboBox.addItem(new QuadCurveMaker());
        comboBox.addItem(new CubicCurveMaker());
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ShapeMaker shapeMaker = (ShapeMaker) comboBox.getSelectedItem();
                panel.setShapeMaker(shapeMaker);
            }
        });
        add(comboBox, BorderLayout.NORTH);
        panel.setShapeMaker((ShapeMaker) comboBox.getItemAt(0));
    }
}


/*
В панели рисуется фигура и организовано перетаскивание ее контрольных точек
 */
class ShapePanel extends JPanel
{
    public ShapePanel()
    {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                for (int i = 0; i < points.length; i++)
                {
                    double x = points[i].getX() - SIZE / 2;
                    double y = points[i].getY() - SIZE / 2;
                    Rectangle2D r = new Rectangle2D.Double(x, y, SIZE, SIZE);
                    if (r.contains(p)){
                        current = i;
                        return;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                current = -1;
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (current == -1) return;
                points[current] = e.getPoint();
                repaint();
            }
        });
        current = -1;
    }

    /*
    Установка объекта, поддерживающего фигуру, и инициализация его
    набором точек с проивольными координатами
    @param aShapeMaker Объект, определяющий фигуру на основе набора точек
     */
    public void setShapeMaker(ShapeMaker ashapeMaker)
    {
        shapeMaker = ashapeMaker;
        int n = shapeMaker.getPointCount();
        points = new Point2D[n];
        for (int i = 0; i < n; i++)
        {
            double x = generator.nextDouble() * getWidth();
            double y = generator.nextDouble() * getHeight();
            points[i] = new Point2D.Double(x, y);
        }
        repaint();
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (points == null) return;
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < points.length; i++)
        {
            double x = points[i].getX() - SIZE / 2;
            double y = points[i].getY() - SIZE / 2;
            g2.fill(new Rectangle2D.Double(x, y, SIZE, SIZE));
        }
        g2.draw(shapeMaker.makeShape(points));
    }

    private Point2D[] points;
    private static Random generator = new Random();
    private static int SIZE = 10;
    private int current;
    private ShapeMaker shapeMaker;
}

/*
Суперкласс классов, определяющих фигуры
Метод makeShape() подклассов возвращает фигуру
 */
abstract class ShapeMaker
{
    /**
     * Конструктор класса фигуры
     * @param aPointCount Число точек, необходимых для определения фигуры
     */
    public ShapeMaker(int aPointCount)
    {
        pointCount = aPointCount;
    }
    /**
     * Данный метод возвращает число точек, необходимых для
     * определения фигуры
     * @return число точек
     */
    public int getPointCount()
    {
        return pointCount;
    }
    /**
     * Создает фигуру на основе заданного набора точек.
     * @param p Точки, определяющие фигуру
     * @return Фигура, определенная посредством точек
     */
    public abstract Shape makeShape(Point2D[] p);

    @Override
    public String toString()
    {
        return getClass().getName();
    }

    private int pointCount;
}

/**
 * Создание линии, соеденяющей 2 точки
 */
class LineMaker extends ShapeMaker
{
    public LineMaker() {super(2);}

    @Override
    public Shape makeShape(Point2D[] p) {
        return new Line2D.Double(p[0], p[1]);
    }
}

/**
 * Создание прямоугольника, определяемого угловыми точками
 */
class RectangleMaker extends ShapeMaker
{
    public RectangleMaker() {super(2);}

    @Override
    public Shape makeShape(Point2D[] p) {
        Rectangle2D s = new Rectangle2D.Double();
        s.setFrameFromDiagonal(p[0], p[1]);
        return s;
    }
}

/**
 * Создание прямоугольника со скругленными углами на основе
 * двух угловых точек.
 */
class RoundRectangleMaker extends ShapeMaker
{
    public RoundRectangleMaker() {super(2);}

    @Override
    public Shape makeShape(Point2D[] p) {
        RoundRectangle2D s = new RoundRectangle2D.Double(0, 0, 0, 0, 20, 20);
        s.setFrameFromDiagonal(p[0], p[1]);
        return s;
    }
}

/**
 * Создание эллипса ограничивающий прямоугольник которого задается посредством
 * двух угловых точек.
 */
class EllipseMaker extends ShapeMaker
{
    public EllipseMaker() {super(2);}

    @Override
    public Shape makeShape(Point2D[] p) {
        Ellipse2D s = new Ellipse2D.Double();
        s.setFrameFromDiagonal(p[0], p[1]);
        return s;
    }
}

/**
 * Создание дуги, ограничивающих прямоугольник которой строиться
 * на основании двух угловых точек. Начальный угол и угол дуги
 * задаются с помощью прямых, проведенных через центр
 * ограничивающего прямоугольника и соответствующую
 * контрольную точку. Для демонстрации корректности
 * процесса построения дуги отображаются ограничивающий
 * прямоугольник и линии, определяющие углы
 */
class ArcMaker extends ShapeMaker
{
    public ArcMaker() {super(4);}

    @Override
    public Shape makeShape(Point2D[] p)
    {
        double centerX = (p[0].getX() + p[1].getX()) / 2;
        double centerY = (p[0].getY() + p[1].getY()) / 2;
        double width = Math.abs(p[1].getX() - p[0].getX());
        double height = Math.abs(p[1].getY() - p[0].getY());

        double distortedStartAngle =
                Math.toDegrees(Math.atan2(-(p[2].getY() - centerY) *
                        width, (p[2].getX() - centerX) * height));
        double distortedEndAngle =
                Math.toDegrees(Math.atan2(-(p[3].getY() - centerY) *
                        width, (p[3].getX() - centerX) * height));
        double distortedAngleDifference =
                distortedEndAngle - distortedStartAngle;
        if (distortedStartAngle < 0) distortedStartAngle += 360;
        if (distortedAngleDifference < 0) distortedAngleDifference += 360;

        Arc2D s = new Arc2D.Double(0, 0, 0, 0, distortedStartAngle,
                distortedAngleDifference, Arc2D.OPEN);
        s.setFrameFromDiagonal(p[0], p[1]);

        GeneralPath g = new GeneralPath();
        g.append(s, false);
        Rectangle2D r = new Rectangle2D.Double();
        r.setFrameFromDiagonal(p[0], p[1]);
        g.append(r, false);
        Point2D center = new Point2D.Double(centerX, centerY);
        g.append(new Line2D.Double(center, p[2]), false);
        g.append(new Line2D.Double(center, p[3]), false);
        return g;
    }
}

/**
 * Создание многоугольника, определяемого шестью угловыми точками.
 */
class PolygonMaker extends ShapeMaker
{
    public PolygonMaker() {super(6);}

    @Override
    public Shape makeShape(Point2D[] p) {
        GeneralPath s = new GeneralPath();
        s.moveTo((float) p[0].getX(), (float) p[0].getY());
        for (int i = 1; i < p.length; i++)
        {
            s.lineTo((float) p[i].getX(), (float) p[i].getY());
        }
        s.closePath();
        return s;
    }
}

/**
 * Создание кривой второго порядка, определенной двумя
 * конечными точками и контрольной точкой
 */
class QuadCurveMaker extends ShapeMaker
{
    public QuadCurveMaker() {super(3);}

    @Override
    public Shape makeShape(Point2D[] p)
    {
        return new QuadCurve2D.Double(p[0].getX(), p[0].getY(),
                p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY());
    }
}

/**
 * Создание кривой третьего порядка, определенной двумя
 * конечными точками и двумя контрольными точками
 */
class CubicCurveMaker extends ShapeMaker
{
    public CubicCurveMaker() {super(4);}

    @Override
    public Shape makeShape(Point2D[] p) {
        return new CubicCurve2D.Double(p[0].getX(), p[0].getY(),
                p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY(),
                p[3].getX(), p[3].getY());
    }
}