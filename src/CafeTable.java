import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

final class CafeTable {
    private static final int SEAT_COUNT = 2;
    private final int index;
    private final int x;
    private final int y;
    private final Customer[] seats = new Customer[SEAT_COUNT];

    CafeTable(int index, int x, int y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }

    int getIndex() {
        return index;
    }

    int getSeatX(int seat) {
        return x + (seat == 0 ? -85 : 85);
    }

    int getSeatY(int seat) {
        return y + 24;
    }

    int findOpenSeat() {
        for (int i = 0; i < seats.length; i++) {
            if (seats[i] == null) {
                return i;
            }
        }
        return -1;
    }

    boolean seat(Customer customer, int seat) {
        if (seat < 0 || seat >= seats.length || seats[seat] != null || customer.isSeated()) {
            return false;
        }
        seats[seat] = customer;
        customer.sitAt(index, seat);
        return true;
    }

    Customer getCustomer(int seat) {
        return seats[seat];
    }

    void unseat(Customer customer) {
        int seat = customer.getSeatIndex();
        if (customer.getTableIndex() == index && seat >= 0 && seats[seat] == customer) {
            seats[seat] = null;
            customer.standUp();
        }
    }

    void drawChairs(Graphics2D g) {
        g.setColor(new Color(151, 111, 77));
        g.fillRoundRect(getSeatX(0) - 20, y + 9, 40, 41, 12, 12);
        g.fillRoundRect(getSeatX(1) - 20, y + 9, 40, 41, 12, 12);
        g.setColor(new Color(184, 139, 96));
        g.fillRoundRect(getSeatX(0) - 15, y + 13, 30, 32, 10, 10);
        g.fillRoundRect(getSeatX(1) - 15, y + 13, 30, 32, 10, 10);
    }

    void drawTabletop(Graphics2D g) {
        g.setColor(new Color(133, 87, 57));
        g.fill(new Ellipse2D.Double(x - 73, y - 10, 146, 52));
        g.setColor(new Color(190, 133, 84));
        g.fill(new Ellipse2D.Double(x - 68, y - 14, 136, 43));
        g.setColor(new Color(224, 179, 126));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Ellipse2D.Double(x - 62, y - 10, 124, 32));
        g.setColor(new Color(105, 130, 90));
        g.fillOval(x - 5, y - 7, 10, 10);
        g.setColor(new Color(238, 210, 170));
        g.fillOval(x - 3, y - 5, 6, 6);
    }
}
