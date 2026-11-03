import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

final class Customer {
    static final int WIDTH = 66;
    static final int HEIGHT = 108;

    private static final Color[] FUR_COLORS = {
        new Color(190, 141, 103),
        new Color(214, 174, 133),
        new Color(154, 119, 91),
        new Color(177, 154, 139),
        new Color(221, 179, 147),
        new Color(145, 127, 102)
    };
    private static final Color[] OUTFIT_COLORS = {
        new Color(112, 153, 113),
        new Color(198, 132, 116),
        new Color(108, 139, 165),
        new Color(198, 157, 91),
        new Color(145, 123, 164),
        new Color(93, 145, 138)
    };
    private static final String[] NAMES = {
        "Pip", "Mochi", "Clover", "Biscuit", "Maple", "Bean", "Miso", "Sunny",
        "Pudding", "Olive", "Noodle", "Peach", "Chai", "Waffle"
    };

    private final int id;
    private final String name;
    private final Color fur;
    private final Color outfit;
    private final int earStyle;
    private int tableIndex = -1;
    private int seatIndex = -1;

    Customer(int id, Random random) {
        this.id = id;
        name = NAMES[random.nextInt(NAMES.length)];
        fur = FUR_COLORS[random.nextInt(FUR_COLORS.length)];
        outfit = OUTFIT_COLORS[random.nextInt(OUTFIT_COLORS.length)];
        earStyle = random.nextInt(3);
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
    }

    boolean isSeated() {
        return tableIndex >= 0;
    }

    int getTableIndex() {
        return tableIndex;
    }

    int getSeatIndex() {
        return seatIndex;
    }

    void sitAt(int table, int seat) {
        tableIndex = table;
        seatIndex = seat;
    }

    void standUp() {
        tableIndex = -1;
        seatIndex = -1;
    }

    void draw(Graphics2D graphics, int centerX, int topY, boolean selected, boolean seated) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(centerX, topY);
        if (selected) {
            g.setColor(new Color(255, 248, 219, 180));
            g.fillOval(-WIDTH / 2 - 7, -5, WIDTH + 14, HEIGHT + 10);
        }

        g.setColor(new Color(73, 62, 51));
        if (earStyle == 0) {
            g.fillOval(-31, 5, 24, 34);
            g.fillOval(7, 5, 24, 34);
        } else if (earStyle == 1) {
            g.fill(new Polygon(new int[] {-29, -23, -8}, new int[] {30, -1, 28}, 3));
            g.fill(new Polygon(new int[] {29, 23, 8}, new int[] {30, -1, 28}, 3));
        } else {
            g.fillRoundRect(-32, 7, 24, 28, 12, 12);
            g.fillRoundRect(8, 7, 24, 28, 12, 12);
        }

        g.setColor(fur);
        if (earStyle == 1) {
            g.fill(new Polygon(new int[] {-26, -23, -13}, new int[] {25, 6, 27}, 3));
            g.fill(new Polygon(new int[] {26, 23, 13}, new int[] {25, 6, 27}, 3));
        } else {
            g.fillOval(-27, 11, 16, 19);
            g.fillOval(11, 11, 16, 19);
        }
        g.setColor(outfit);
        g.fillRoundRect(-25, 56, 50, 49, 18, 18);
        g.setColor(new Color(255, 233, 207));
        g.fillOval(-26, 25, 52, 48);
        g.setColor(fur);
        g.fillOval(-29, 14, 58, 52);
        g.setColor(new Color(255, 233, 207));
        g.fillOval(-23, 29, 46, 39);

        g.setColor(new Color(71, 58, 49));
        g.fillOval(-14, 42, 5, 7);
        g.fillOval(9, 42, 5, 7);
        g.setColor(new Color(226, 145, 130, 180));
        g.fillOval(-22, 52, 9, 5);
        g.fillOval(13, 52, 9, 5);
        g.setColor(new Color(128, 74, 68));
        g.fillOval(-3, 54, 6, 4);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(-6, 56, 12, 8, 0, -180);

        g.setColor(new Color(255, 234, 207));
        g.fillRoundRect(-8, 65, 16, 17, 7, 7);
        g.setColor(new Color(109, 77, 55));
        g.fillOval(-5, 68, 10, 4);
        g.setColor(new Color(249, 242, 222));
        g.drawLine(-17, 89, -12, 98);
        g.drawLine(17, 89, 12, 98);
        if (seated) {
            g.setColor(new Color(255, 248, 231));
            g.fillRoundRect(-26, 73, 17, 12, 3, 3);
            g.setColor(new Color(138, 91, 61));
            g.fillOval(-23, 75, 11, 4);
        }
        g.dispose();
    }
}
