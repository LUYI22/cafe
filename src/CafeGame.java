import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

final class CafeGame {
    private static final Color CREAM = new Color(255, 248, 231);
    private static final Color INK = new Color(69, 61, 52);

    private final JFrame frame = new JFrame("Mallow & Mint Café");
    private final CafeShop shop = new CafeShop();
    private final JLabel dayLabel = new JLabel("Your café is ready");
    private final JLabel guestLabel = new JLabel("Today's guests: —");
    private final JLabel waitingLabel = new JLabel("Waiting: 0");
    private final JLabel seatedLabel = new JLabel("Seated: 0");
    private final JLabel servedLabel = new JLabel("Served: 0");
    private final JLabel salesLabel = new JLabel("Sales: $0");
    private final JLabel messageLabel = new JLabel("Open the shop, then drag each neighbor to a chair.");
    private final JButton openButton = new JButton("Open the shop");
    private final JButton serveButton = new JButton("Serve seated guest");
    private final JButton closeButton = new JButton("Close for the day");
    private boolean open;
    private int day;
    private int totalGuests;
    private int served;
    private int sales;
    private Random dayRandom = new Random();

    CafeGame() {
        shop.setOnChange(this::refresh);
    }

    void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(940, 780));
        frame.setSize(1120, 900);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(shop, BorderLayout.CENTER);
        root.add(createDashboard(), BorderLayout.SOUTH);
        frame.setContentPane(root);

        openButton.addActionListener(event -> openShop());
        serveButton.addActionListener(event -> serveGuest());
        closeButton.addActionListener(event -> closeShop());
        refresh();
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 3));
        titles.setOpaque(false);
        JLabel title = new JLabel("Mallow & Mint Café");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(INK);
        JLabel subtitle = new JLabel("A little neighborhood hideaway for hungry friends");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(119, 105, 88));
        titles.add(title);
        titles.add(subtitle);

        dayLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        dayLabel.setForeground(new Color(75, 111, 81));
        dayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(titles, BorderLayout.WEST);
        header.add(dayLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel createDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(12, 8));
        dashboard.setBackground(Color.WHITE);
        dashboard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(232, 221, 201), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel stats = new JPanel(new BorderLayout(4, 6));
        stats.setOpaque(false);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        messageLabel.setForeground(INK);
        JPanel counts = new JPanel(new GridLayout(1, 5, 12, 0));
        counts.setOpaque(false);
        for (JLabel label : List.of(guestLabel, waitingLabel, seatedLabel, servedLabel, salesLabel)) {
            label.setForeground(INK);
            counts.add(label);
        }
        salesLabel.setForeground(new Color(75, 111, 81));
        stats.add(messageLabel, BorderLayout.NORTH);
        stats.add(counts, BorderLayout.SOUTH);

        styleButton(openButton, new Color(93, 135, 101));
        styleButton(serveButton, new Color(214, 151, 78));
        styleButton(closeButton, new Color(116, 105, 91));
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(openButton);
        buttons.add(serveButton);
        buttons.add(closeButton);

        dashboard.add(stats, BorderLayout.CENTER);
        dashboard.add(buttons, BorderLayout.EAST);
        return dashboard;
    }

    private void styleButton(JButton button, Color background) {
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    }

    private void openShop() {
        day++;
        dayRandom = new Random(ThreadLocalRandom.current().nextLong());
        totalGuests = 8 + dayRandom.nextInt(7);
        served = 0;
        sales = 0;
        open = true;
        List<Customer> guests = new ArrayList<>();
        for (int i = 0; i < totalGuests; i++) {
            guests.add(new Customer(i, dayRandom));
        }
        shop.startDay(guests);
        messageLabel.setText("Drag a neighbor to an empty chair, then serve them a treat.");
        refresh();
    }

    private void serveGuest() {
        if (!open) {
            return;
        }
        Customer customer = shop.serveNextCustomer();
        if (customer == null) {
            messageLabel.setText("Seat a guest before serving them.");
            return;
        }
        served++;
        sales += 4 + dayRandom.nextInt(9);
        messageLabel.setText(served == totalGuests
                ? "Everyone's been served! What a lovely day."
                : "A happy guest, a warm cup, and one more sale.");
        refresh();
    }

    private void closeShop() {
        if (!open) {
            return;
        }
        open = false;
        shop.closeDay();
        messageLabel.setText("Closed for the day. You served " + served + " of " + totalGuests + " neighbors.");
        refresh();
    }

    private void refresh() {
        int waiting = shop.getWaitingCount();
        int seated = shop.getSeatedCount();
        openButton.setEnabled(!open);
        serveButton.setEnabled(open && seated > 0);
        closeButton.setEnabled(open);
        dayLabel.setText(day == 0 ? "Your café is ready" : "Day " + day + (open ? "  ·  Open" : "  ·  Closed"));
        guestLabel.setText(day == 0 ? "Today's guests: —" : "Today's guests: " + totalGuests);
        waitingLabel.setText("Waiting: " + waiting);
        seatedLabel.setText("Seated: " + seated);
        servedLabel.setText("Served: " + served);
        salesLabel.setText("Sales: $" + sales);
    }
}
