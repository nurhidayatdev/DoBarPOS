package dobarpos;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends javax.swing.JFrame {

    // Teks placeholder
    private static final String PH_USER = "Masukkan username...";
    private static final String PH_PASS = "Masukkan password...";
    private boolean passIsPlaceholder = true;

    public LoginFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        setupPlaceholders();
        setupLogo();
    }

    private void setupLogo() {
        try {
            java.net.URL imgURL = getClass().getResource("/dobarpos/assets/logo.png");
            if (imgURL != null) {
                java.awt.Image img = javax.imageio.ImageIO.read(imgURL);
                java.awt.Image scaled = img.getScaledInstance(80, 80, java.awt.Image.SCALE_SMOOTH);
                jLabel8.setIcon(new javax.swing.ImageIcon(scaled));
                jLabel8.setText(""); // Hapus placeholder emoji
                jPanel4.setOpaque(false); // Buat background transparan
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat logo: " + e.getMessage());
        }
    }

    /** Pasang placeholder pada txtUsername dan txtPassword */
    private void setupPlaceholders() {
        // ── txtUsername placeholder ───────────────────────────────
        txtUsername.setText(PH_USER);
        txtUsername.setForeground(new Color(170, 170, 170));
        txtUsername.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 14));
        txtUsername.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (txtUsername.getText().equals(PH_USER)) {
                    txtUsername.setText("");
                    txtUsername.setForeground(Color.BLACK);
                    txtUsername.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (txtUsername.getText().isEmpty()) {
                    txtUsername.setText(PH_USER);
                    txtUsername.setForeground(new Color(170, 170, 170));
                    txtUsername.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 14));
                }
            }
        });

        // ── txtPassword placeholder ───────────────────────────────
        // JPasswordField tidak support setEchoChar('\0') di semua versi,
        // gunakan teks biasa saat placeholder, ganti ke echo saat focus masuk.
        txtPassword.setEchoChar((char) 0);      // tampilkan teks placeholder
        txtPassword.setText(PH_PASS);
        txtPassword.setForeground(new Color(170, 170, 170));
        txtPassword.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 14));
        passIsPlaceholder = true;
        txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (passIsPlaceholder) {
                    txtPassword.setText("");
                    txtPassword.setEchoChar('\u2022'); // bullet •
                    txtPassword.setForeground(Color.BLACK);
                    txtPassword.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
                    passIsPlaceholder = false;
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (txtPassword.getPassword().length == 0) {
                    txtPassword.setEchoChar((char) 0);
                    txtPassword.setText(PH_PASS);
                    txtPassword.setForeground(new Color(170, 170, 170));
                    txtPassword.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 14));
                    passIsPlaceholder = true;
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Login");

        jPanel1.setBackground(new java.awt.Color(252, 246, 246));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 560));
        jPanel2.setLayout(null);

        jPanel3.setBackground(new java.awt.Color(139, 29, 36));
        jPanel2.add(jPanel3);
        jPanel3.setBounds(0, 0, 400, 20);

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("DoBarPOS");
        jPanel2.add(jLabel1);
        jLabel1.setBounds(0, 130, 400, 32);

        jLabel2.setForeground(new java.awt.Color(138, 138, 138));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Aplikasi Kasir Digital");
        jPanel2.add(jLabel2);
        jLabel2.setBounds(0, 160, 400, 16);

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(138, 138, 138));
        jLabel3.setText("Username");
        jPanel2.add(jLabel3);
        jLabel3.setBounds(40, 200, 320, 16);

        txtUsername.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtUsername.setText("admin_user");
        txtUsername.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        jPanel2.add(txtUsername);
        txtUsername.setBounds(40, 220, 320, 35);

        jLabel4.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(138, 138, 138));
        jLabel4.setText("Password");
        jPanel2.add(jLabel4);
        jLabel4.setBounds(40, 270, 320, 16);

        txtPassword.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtPassword.setText("password123");
        txtPassword.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        jPanel2.add(txtPassword);
        txtPassword.setBounds(40, 290, 320, 35);

        btnLogin.setBackground(new java.awt.Color(139, 29, 36));
        btnLogin.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("Login");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        jPanel2.add(btnLogin);
        btnLogin.setBounds(40, 350, 320, 40);

        jLabel6.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(138, 138, 138));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("© 2026 DoBarPOS by Kelompok 7");
        jPanel2.add(jLabel6);
        jLabel6.setBounds(0, 500, 400, 14);

        jLabel7.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(138, 138, 138));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Teknik Informatika dan Komputer FT-UNM");
        jPanel2.add(jLabel7);
        jLabel7.setBounds(0, 520, 400, 14);

        jPanel4.setBackground(new java.awt.Color(248, 230, 230));
        jPanel4.setLayout(null);

        jLabel8.setFont(new java.awt.Font("Segoe UI Emoji", 0, 24)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("🏪");
        jPanel4.add(jLabel8);
        jLabel8.setBounds(0, 0, 80, 80);

        jPanel2.add(jPanel4);
        jPanel4.setBounds(160, 40, 80, 80);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jPanel2, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        // Validasi: jangan lolos jika field masih berisi teks placeholder
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || username.equals(PH_USER)
                || password.isEmpty() || passIsPlaceholder) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Username dan Password tidak boleh kosong!",
                "Perhatian", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Autentikasi ke MySQL ──────────────────────────────────
        // Query: SELECT id_user, username, role FROM user
        //        WHERE username = ? AND password = SHA2(?, 256)
        boolean loginBerhasil = UserSession.login(username, password);

        if (loginBerhasil) {
            String role = UserSession.getInstance().getRole();
            String namaUser = UserSession.getInstance().getUsername();

            System.out.println("Login berhasil: " + namaUser + " (" + role + ")");

            // Routing berdasarkan Role
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (role.equals("Kasir")) {
                    new TransaksiFrame().setVisible(true);
                } else if (role.equals("Manager")) {
                    new LaporanFrame().setVisible(true);
                } else {
                    new DashboardFrame().setVisible(true);
                }
            });
            this.dispose(); // Tutup LoginFrame

        } else {
            // Login gagal — tampilkan pesan error
            javax.swing.JOptionPane.showMessageDialog(this,
                "Username atau Password salah!\nSilakan coba lagi.",
                "Login Gagal", javax.swing.JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
