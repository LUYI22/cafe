import javax.swing.SwingUtilities;

public class cafe {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CafeGame().show());
    }
}
