import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
final class CafeShop extends JPanel {
    private static final int SCENE_WIDTH = 960;
    private static final int SCENE_HEIGHT = 630;
    private static final int CUSTOMER_HEIGHT = Customer.HEIGHT;

    private final List<CafeTable> tables = new ArrayList<>();
    private final List<Customer> guests = new ArrayList<>();
    private final JLabel hint = new JLabel("Your shop is closed. Open it to welcome the neighborhood.", SwingConstants.CENTER);
    private Runnable onChange = () -> {};
    private Customer draggedCustomer;
    private int dragCenterX;
    private int dragTopY;
    private int grabOffsetX;
    private int grabOffsetY;
    private boolean open;

    CafeShop() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(245, 234, 215));
        setPreferredSize(new Dimension(SCENE_WIDTH, SCENE_HEIGHT + 35));
        setBorder(BorderFactory.createLineBorder(new Color(232, 221, 201), 1, true));

        tables.add(new CafeTable(0, 445, 330));
        tables.add(new CafeTable(1, 740, 330));
        tables.add(new CafeTable(2, 445, 505));
        tables.add(new CafeTable(3, 740, 505));

        hint.setOpaque(true);
        hint.setBackground(new Color(255, 251, 241));
        hint.setForeground(new Color(94, 82, 68));
        hint.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(hint, BorderLayout.SOUTH);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                beginDrag(event);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                continueDrag(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                finishDrag(event);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    void setOnChange(Runnable callback) {
        onChange = callback;
    }

    void startDay(List<Customer> newGuests) {
        for (CafeTable table : tables) {
            for (int seat = 0; seat < 2; seat++) {
                Customer customer = table.getCustomer(seat);
                if (customer != null) {
                    table.unseat(customer);
                }
            }
        }
        guests.clear();
        guests.addAll(newGuests);
        open = true;
        draggedCustomer = null;
        hint.setText("Drag a neighbor to an empty chair. Serve guests after they sit down.");
        repaint();
    }

    void closeDay() {
        for (CafeTable table : tables) {
            for (int seat = 0; seat < 2; seat++) {
                Customer customer = table.getCustomer(seat);
                if (customer != null) {
                    table.unseat(customer);
                }
            }
        }
        guests.clear();
        draggedCustomer = null;
        open = false;
        hint.setText("The café is closed. Your tables are ready for a new day.");
        repaint();
    }

    int getWaitingCount() {
        int waiting = 0;
        for (Customer customer : guests) {
            if (!customer.isSeated()) {
                waiting++;
            }
        }
        return waiting;
    }

    int getSeatedCount() {
        int seated = 0;
        for (Customer customer : guests) {
            if (customer.isSeated()) {
                seated++;
            }
        }
        return seated;
    }

    Customer serveNextCustomer() {
        for (int i = 0; i < guests.size(); i++) {
            Customer customer = guests.get(i);
            if (customer.isSeated()) {
                tables.get(customer.getTableIndex()).unseat(customer);
                guests.remove(i);
                repaint();
                return customer;
            }
        }
        return null;
    }

    private void beginDrag(MouseEvent event) {
        if (!open || guests.isEmpty()) {
            return;
        }
        int[] point = scenePoint(event.getX(), event.getY());
        if (point == null) {
            return;
        }
        List<Customer> waiting = getWaitingGuests();
        for (int i = Math.min(waiting.size(), 4) - 1; i >= 0; i--) {
            int[] position = waitingPosition(i);
            int left = position[0] - Customer.WIDTH / 2;
            if (point[0] >= left && point[0] <= left + Customer.WIDTH
                    && point[1] >= position[1] && point[1] <= position[1] + CUSTOMER_HEIGHT) {
                draggedCustomer = waiting.get(i);
                dragCenterX = position[0];
                dragTopY = position[1];
                grabOffsetX = point[0] - dragCenterX;
                grabOffsetY = point[1] - dragTopY;
                repaint();
                return;
            }
        }
    }

    private void continueDrag(MouseEvent event) {
        if (draggedCustomer == null) {
            return;
        }
        int[] point = scenePoint(event.getX(), event.getY());
        if (point == null) {
            return;
        }
        dragCenterX = point[0] - grabOffsetX;
        dragTopY = point[1] - grabOffsetY;
        repaint();
    }

    private void finishDrag(MouseEvent event) {
        if (draggedCustomer == null) {
            return;
        }
        int[] point = scenePoint(event.getX(), event.getY());
        if (point != null) {
            int seatTable = -1;
            int seatIndex = -1;
            int bestDistance = 79 * 79;
            for (CafeTable table : tables) {
                for (int seat = 0; seat < 2; seat++) {
                    if (table.getCustomer(seat) != null) {
                        continue;
                    }
                    int dx = point[0] - table.getSeatX(seat);
                    int dy = point[1] - table.getSeatY(seat);
                    int distance = dx * dx + dy * dy;
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        seatTable = table.getIndex();
                        seatIndex = seat;
                    }
                }
            }
            if (seatTable >= 0 && tables.get(seatTable).seat(draggedCustomer, seatIndex)) {
                hint.setText(draggedCustomer.getName() + " found a seat. Ready to take their order!");
                draggedCustomer = null;
                repaint();
                onChange.run();
                return;
            }
            hint.setText("Drop your neighbor beside an empty chair to seat them.");
        }
        draggedCustomer = null;
        repaint();
    }

    private int[] scenePoint(int componentX, int componentY) {
        int availableHeight = getHeight() - hint.getHeight();
        double scale = Math.min(getWidth() / (double) SCENE_WIDTH, availableHeight / (double) SCENE_HEIGHT);
        if (scale <= 0) {
            return null;
        }
        int offsetX = (int) ((getWidth() - SCENE_WIDTH * scale) / 2);
        int offsetY = (int) ((availableHeight - SCENE_HEIGHT * scale) / 2);
        int x = (int) ((componentX - offsetX) / scale);
        int y = (int) ((componentY - offsetY) / scale);
        if (x < 0 || x >= SCENE_WIDTH || y < 0 || y >= SCENE_HEIGHT) {
            return null;
        }
        return new int[] {x, y};
    }

    private List<Customer> getWaitingGuests() {
        List<Customer> waiting = new ArrayList<>();
        for (Customer customer : guests) {
            if (!customer.isSeated() && customer != draggedCustomer) {
                waiting.add(customer);
            }
        }
        if (draggedCustomer != null && !draggedCustomer.isSeated()) {
            waiting.add(0, draggedCustomer);
        }
        return waiting;
    }

    private int[] waitingPosition(int index) {
        int column = index % 2;
        int row = index / 2;
        return new int[] {90 + column * 106, 266 + row * 150};
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int availableHeight = getHeight() - hint.getHeight();
        double scale = Math.min(getWidth() / (double) SCENE_WIDTH, availableHeight / (double) SCENE_HEIGHT);
        double offsetX = (getWidth() - SCENE_WIDTH * scale) / 2;
        double offsetY = (availableHeight - SCENE_HEIGHT * scale) / 2;
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
        drawRoom(g);
        drawQueue(g);
        drawFurniture(g);
        drawSeatedCustomers(g);
        drawTabletops(g);
        drawDraggedCustomer(g);
        drawWelcomeBoard(g);
        g.dispose();
    }

    private void drawRoom(Graphics2D g) {
        g.setColor(new Color(248, 237, 218));
        g.fillRect(0, 0, SCENE_WIDTH, 213);
        g.setColor(new Color(211, 170, 125));
        g.fillRect(0, 213, SCENE_WIDTH, SCENE_HEIGHT - 213);
        g.setColor(new Color(198, 150, 106));
        for (int x = 0; x < SCENE_WIDTH; x += 96) {
            g.fillRect(x, 213, 2, SCENE_HEIGHT - 213);
        }
        g.setColor(new Color(198, 150, 106));
        for (int y = 266; y < SCENE_HEIGHT; y += 86) {
            g.fillRect(0, y, SCENE_WIDTH, 2);
        }

        g.setColor(new Color(255, 250, 239));
        g.fillRoundRect(22, 20, 916, 180, 26, 26);
        drawWindow(g, 50, 49);
        drawSign(g);
        drawCounter(g);

        g.setColor(new Color(176, 127, 86));
        g.fillRoundRect(828, 143, 79, 77, 8, 8);
        g.setColor(new Color(239, 218, 180));
        g.fillRoundRect(835, 151, 65, 59, 5, 5);
        g.setColor(new Color(108, 119, 82));
        drawCentered(g, "WELCOME", 867, 176, new Font("SansSerif", Font.BOLD, 10));
        drawCentered(g, "come on in", 867, 194, new Font("Serif", Font.ITALIC, 11));
    }

    private void drawWindow(Graphics2D g, int x, int y) {
        g.setColor(new Color(148, 105, 73));
        g.fillRoundRect(x - 6, y - 5, 164, 126, 16, 16);
        g.setColor(new Color(181, 222, 216));
        g.fillRoundRect(x, y, 152, 114, 12, 12);
        g.setColor(new Color(255, 235, 181));
        g.fillOval(x + 99, y + 13, 30, 30);
        g.setColor(new Color(151, 199, 171));
        g.fillOval(x + 10, y + 70, 77, 33);
        g.fillOval(x + 65, y + 61, 78, 42);
        g.setColor(new Color(255, 248, 231));
        g.fillRect(x + 73, y + 2, 7, 110);
        g.fillRect(x + 2, y + 54, 148, 7);
    }

    private void drawSign(Graphics2D g) {
        g.setColor(new Color(91, 119, 82));
        g.fillRoundRect(338, 40, 250, 72, 22, 22);
        g.setColor(new Color(216, 193, 139));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(346, 48, 234, 56, 18, 18);
        g.setColor(new Color(255, 244, 210));
        drawCentered(g, "MALLOW & MINT", 463, 75, new Font("SansSerif", Font.BOLD, 18));
        drawCentered(g, "CAFÉ", 463, 98, new Font("Serif", Font.ITALIC, 16));
    }

    private void drawCounter(Graphics2D g) {
        g.setColor(new Color(132, 89, 59));
        g.fillRoundRect(315, 149, 471, 56, 16, 16);
        g.setColor(new Color(190, 134, 83));
        g.fillRoundRect(305, 138, 490, 28, 12, 12);
        g.setColor(new Color(240, 216, 180));
        drawCentered(g, "ORDER HERE", 550, 157, new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(255, 248, 229));
        g.fillRoundRect(343, 119, 102, 20, 9, 9);
        g.setColor(new Color(126, 87, 58));
        drawCentered(g, "fresh pastries", 394, 133, new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(107, 145, 101));
        g.fillOval(735, 113, 24, 27);
        g.fillOval(751, 106, 22, 30);
        g.setColor(new Color(181, 120, 83));
        g.fillRoundRect(739, 132, 31, 16, 5, 5);
    }

    private void drawQueue(Graphics2D g) {
        g.setColor(new Color(255, 249, 235, 220));
        g.fillRoundRect(27, 226, 227, 383, 20, 20);
        g.setColor(new Color(211, 188, 151));
        g.setStroke(new BasicStroke(1.4f));
        g.drawRoundRect(27, 226, 227, 383, 20, 20);
        g.setColor(new Color(96, 123, 87));
        drawCentered(g, "WAITING TO BE SEATED", 140, 250, new Font("SansSerif", Font.BOLD, 12));

        List<Customer> waiting = getWaitingGuests();
        int visible = Math.min(waiting.size(), 4);
        for (int i = 0; i < visible; i++) {
            Customer customer = waiting.get(i);
            int[] position = waitingPosition(i);
            if (customer != draggedCustomer) {
                customer.draw(g, position[0], position[1], false, false);
                g.setColor(new Color(95, 78, 61));
                drawCentered(g, customer.getName(), position[0], position[1] + Customer.HEIGHT + 4,
                        new Font("SansSerif", Font.PLAIN, 11));
            }
        }
        if (waiting.size() > visible) {
            g.setColor(new Color(112, 93, 73));
            drawCentered(g, "+" + (waiting.size() - visible) + " more in line", 140, 583,
                    new Font("SansSerif", Font.BOLD, 11));
        }
    }

    private void drawFurniture(Graphics2D g) {
        for (CafeTable table : tables) {
            table.drawChairs(g);
        }
    }

    private void drawSeatedCustomers(Graphics2D g) {
        for (CafeTable table : tables) {
            for (int seat = 0; seat < 2; seat++) {
                Customer customer = table.getCustomer(seat);
                if (customer != null && customer != draggedCustomer) {
                    customer.draw(g, table.getSeatX(seat), table.getSeatY(seat) - CUSTOMER_HEIGHT, false, true);
                }
            }
        }
    }

    private void drawTabletops(Graphics2D g) {
        for (CafeTable table : tables) {
            table.drawTabletop(g);
        }
    }

    private void drawDraggedCustomer(Graphics2D g) {
        if (draggedCustomer == null) {
            return;
        }
        draggedCustomer.draw(g, dragCenterX, dragTopY, true, false);
    }

    private void drawWelcomeBoard(Graphics2D g) {
        g.setColor(new Color(145, 104, 71));
        g.fillRoundRect(281, 218, 163, 57, 8, 8);
        g.setColor(new Color(68, 83, 64));
        g.fillRoundRect(287, 224, 151, 45, 5, 5);
        g.setColor(new Color(255, 242, 207));
        drawCentered(g, "TODAY'S LITTLE MENU", 362, 242, new Font("SansSerif", Font.BOLD, 10));
        drawCentered(g, "latte $5  ·  cake $6", 362, 259, new Font("Serif", Font.PLAIN, 11));

        g.setColor(new Color(255, 251, 241, 238));
        g.fill(new RoundRectangle2D.Float(466, 224, 351, 40, 18, 18));
        g.setColor(new Color(211, 188, 151));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Float(466, 224, 351, 40, 18, 18));
        String text = !open ? "A neighborhood café, ready to welcome you"
                : guests.isEmpty() ? "Everyone has been served - lovely work!"
                : getWaitingCount() == 0 ? "All tables are full - serve a guest for the next neighbor"
                : getWaitingCount() + (getWaitingCount() == 1 ? " neighbor is waiting for a seat"
                        : " neighbors are waiting for seats");
        g.setColor(new Color(69, 61, 52));
        drawCentered(g, text, 641, 249, new Font("SansSerif", Font.BOLD, 13));
    }

    private static void drawCentered(Graphics2D g, String text, int x, int y, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, x - metrics.stringWidth(text) / 2, y);
    }
}
