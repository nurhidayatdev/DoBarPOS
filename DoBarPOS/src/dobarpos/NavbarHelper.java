package dobarpos;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * NavbarHelper - Standarisasi tampilan navbar (header) di seluruh halaman DoBarPOS.
 * Menjamin konsistensi search field, profil user (Name + Role), dan tombol logout.
 */
public class NavbarHelper {

    /**
     * Mengkonfigurasi seluruh panel header agar sesuai dengan desain premium di screenshot.
     * 
     * @param headerPanel  Panel utama header (BorderLayout)
     * @param searchField  TextField untuk pencarian
     * @param profileLabel Label profil (digunakan sebagai referensi, namun info diambil dari session)
     * @param btnLogout    Tombol logout
     * @param placeholder  Teks placeholder untuk search bar (misal "Cari menu...")
     */
    public static void setupFullNavbar(JPanel headerPanel, JTextField searchField, JLabel profileLabel, JButton btnLogout, String placeholder) {
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(1000, 75)); // Sedikit lebih tinggi agar tidak sesak
        headerPanel.setLayout(new BorderLayout());
        headerPanel.removeAll();

        // ── BAGIAN KIRI: SEARCH BAR ──────────────────────────────────────
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 16));
        leftPanel.setOpaque(false);
        
        searchField.setVisible(true); // Pastikan terlihat
        applySearchStyle(searchField, placeholder);
        leftPanel.add(searchField);
        headerPanel.add(leftPanel, BorderLayout.WEST);

        // ── BAGIAN KANAN: PROFILE & LOGOUT ────────────────────────────────
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        rightPanel.setOpaque(false);

        // 1. User Info (Name & Role stacked)
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        
        String name = UserSession.getInstance().getUsername();
        String role = UserSession.getInstance().getRole();
        if (name == null) name = "Guest";
        if (role == null) role = "User";

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblName.setForeground(new Color(30, 30, 30));
        lblName.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblRole = new JLabel(role);
        lblRole.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRole.setForeground(new Color(120, 120, 120));
        lblRole.setAlignmentX(Component.RIGHT_ALIGNMENT);

        userInfoPanel.add(lblName);
        userInfoPanel.add(lblRole);
        rightPanel.add(userInfoPanel);

        // 2. Profile Icon (Circular look)
        JLabel profileIcon = new JLabel("\uD83D\uDC64"); // 👤 icon
        profileIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        profileIcon.setForeground(new Color(139, 29, 36));
        profileIcon.setBorder(new EmptyBorder(0, 5, 0, 10));
        rightPanel.add(profileIcon);

        // 3. Vertical Divider
        JSeparator divider = new JSeparator(SwingConstants.VERTICAL);
        divider.setPreferredSize(new Dimension(1, 30));
        divider.setForeground(new Color(230, 230, 230));
        rightPanel.add(divider);

        // 4. Logout Button
        btnLogout.setText("\u21AA Logout"); // ↪ Logout
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogout.setForeground(new Color(139, 29, 36));
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Kurangi padding agar muat
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setContentAreaFilled(false);
        // Berikan lebar minimum agar teks tidak terpotong (terutama di layar sempit)
        btnLogout.setPreferredSize(new Dimension(110, 40)); 
        
        rightPanel.add(btnLogout);

        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        headerPanel.revalidate();
        headerPanel.repaint();
        
        // Force parent refresh if exists
        if (headerPanel.getParent() != null) {
            headerPanel.getParent().revalidate();
            headerPanel.getParent().repaint();
        }
    }

    public static void applySearchStyle(JTextField field, String placeholder) {
        field.setPreferredSize(new Dimension(420, 42));
        field.setText(placeholder);
        field.setForeground(new Color(150, 150, 150));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Rounded border with pinkish background
        field.setBackground(new Color(252, 241, 241));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, new Color(230, 210, 210)),
            BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));

        // Hapus listener lama jika ada (untuk menghindari double listeners saat ganti placeholder)
        for (java.awt.event.FocusListener fl : field.getFocusListeners()) {
            field.removeFocusListener(fl);
        }

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(30, 30, 30));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setForeground(new Color(150, 150, 150));
                    field.setText(placeholder);
                }
            }
        });
    }

    // Custom Rounded Border class
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private int radius;
        private Color color;
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    // Compatibility methods for existing calls
    public static void applyLogoutStyle(JButton btnLogout) {
        btnLogout.setForeground(new Color(139, 29, 36));
        btnLogout.setContentAreaFilled(false);
    }

    public static void applyProfileStyle(JLabel lblProfile) {
    }

    // Helper untuk update placeholder saja
    public static void setupFullNavbar(JPanel headerPanel, JTextField searchField, JLabel profileLabel, JButton btnLogout) {
        setupFullNavbar(headerPanel, searchField, profileLabel, btnLogout, "Search POS data...");
    }
}
