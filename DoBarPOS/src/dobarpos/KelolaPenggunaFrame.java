package dobarpos;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

public class KelolaPenggunaFrame extends javax.swing.JFrame {

    private int selectedUserId = -1;
    private DefaultTableModel tableModel;

    public KelolaPenggunaFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        tbl_pengguna.setRowHeight(40);
        tbl_pengguna.setShowGrid(false);
        tbl_pengguna.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tbl_pengguna.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        tbl_pengguna.getTableHeader().setOpaque(false);
        tbl_pengguna.getTableHeader().setBackground(new Color(252, 246, 246));
        tbl_pengguna.setSelectionBackground(new Color(252, 225, 225));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        btnMenuKelolaPengguna.setBackground(Color.WHITE);
        btnMenuKelolaPengguna.setForeground(new Color(139, 29, 36));
        lblProfile.setText(UserSession.getInstance().getUsername() + " - " + UserSession.getInstance().getRole());

        // Init tableModel
        tableModel = new DefaultTableModel(
            new String[]{"ID USER", "USERNAME", "PERAN"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int c) {
                return (c == 0) ? Integer.class : String.class;
            }
        };
        tbl_pengguna.setModel(tableModel);

        TableColumnModel colModel = tbl_pengguna.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(60);
        colModel.getColumn(1).setPreferredWidth(250);
        colModel.getColumn(2).setPreferredWidth(100);
        colModel.getColumn(0).setCellRenderer(centerRenderer);
        colModel.getColumn(2).setCellRenderer(centerRenderer);

        loadDataPengguna();

        // Tambah tombol Riwayat & Laporan ke Sidebar secara dinamis
        javax.swing.JButton btnMenuRiwayat = new javax.swing.JButton("🕐  Riwayat");
        btnMenuRiwayat.setBackground(new java.awt.Color(139, 29, 36));
        btnMenuRiwayat.setFont(new java.awt.Font("SansSerif", 1, 14));
        btnMenuRiwayat.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuRiwayat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuRiwayat.setBounds(10, 350, 230, 40);
        jPanelSidebar.add(btnMenuRiwayat);

        javax.swing.JButton btnMenuLaporan = new javax.swing.JButton("📊  Laporan");
        btnMenuLaporan.setBackground(new java.awt.Color(139, 29, 36));
        btnMenuLaporan.setFont(new java.awt.Font("SansSerif", 1, 14));
        btnMenuLaporan.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuLaporan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuLaporan.setBounds(10, 400, 230, 40);
        jPanelSidebar.add(btnMenuLaporan);

        // RBAC Sidebar
        Map<String, JButton> sidebarMap = new LinkedHashMap<>();
        sidebarMap.put("Dashboard",     btnMenuDashboard);
        sidebarMap.put("KelolaMenu",    btnMenuKelolaMenu);
        sidebarMap.put("KelolaStok",    btnMenuKelolaStok);
        sidebarMap.put("KelolaPengguna",btnMenuKelolaPengguna);
        sidebarMap.put("Transaksi",     btnMenuTransaksi);
        sidebarMap.put("Riwayat",       btnMenuRiwayat);
        sidebarMap.put("Laporan",       btnMenuLaporan);
        NavigationHelper.configureSidebar(sidebarMap);
        NavigationHelper.setActiveButton(btnMenuKelolaPengguna,
            btnMenuDashboard, btnMenuKelolaMenu, btnMenuKelolaStok,
            btnMenuKelolaPengguna, btnMenuTransaksi, btnMenuRiwayat, btnMenuLaporan);

        btnMenuDashboard.addActionListener(e -> NavigationHelper.navigateTo(this, "Dashboard"));
        btnMenuKelolaMenu.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaMenu"));
        btnMenuKelolaStok.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaStok"));
        btnMenuKelolaPengguna.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaPengguna"));
        btnMenuTransaksi.addActionListener(e -> NavigationHelper.navigateTo(this, "Transaksi"));
        btnMenuRiwayat.addActionListener(e -> NavigationHelper.navigateTo(this, "Riwayat"));
        btnMenuLaporan.addActionListener(e -> NavigationHelper.navigateTo(this, "Laporan"));
        btnLogout.addActionListener(e -> NavigationHelper.logout(this));

        // Standarisasi Navbar menggunakan NavbarHelper
        NavbarHelper.setupFullNavbar(jPanelHeader, txtSearchHeader, lblProfile, btnLogout, "Cari staf atau admin...");

        // Sort & Search Logic
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tbl_pengguna.setRowSorter(sorter);
        // Default sort by ID (column 0)
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);

        txtSearchHeader.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            private void filterTable() {
                String text = txtSearchHeader.getText();
                if (text.equals("Cari staf atau admin...") || text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        // Layout dinamis — isi penuh seperti LaporanFrame
        jPanelContentWrapper.setLayout(new java.awt.BorderLayout());
        jPanelContentWrapper.removeAll();
        jPanelContentWrapper.add(jPanelContent, java.awt.BorderLayout.CENTER);
        jPanelContent.setPreferredSize(null);
        jPanelContent.setMinimumSize(null);
        jPanelContent.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int W = jPanelContent.getWidth();
                int pad = 30;
                int formW = 310;
                int tableW = W - pad * 2 - formW - 20;
                lblDashTitle.setBounds(pad, 20, W - pad * 2, 37);
                lblDashSub.setBounds(pad, 60, W - pad * 2, 19);
                jPanelForm.setBounds(pad, 110, formW, 430);
                jPanelTable.setBounds(pad + formW + 20, 110, tableW, 430);
                jScrollPane1.setBounds(20, 60, tableW - 40, 355);
            }
        });
    }

    private void loadDataPengguna() {
        tableModel.setRowCount(0);
        String sql = "SELECT id_user, username, role FROM user ORDER BY id_user ASC";
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_user"),
                    rs.getString("username"),
                    rs.getString("role")
                });
            }
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal load data pengguna: " + e.getMessage());
        }
    }

    /** Mengosongkan semua field form setelah aksi berhasil */
    private void clearForm() {
        txt_username.setText("");
        txt_password.setText("");
        cb_role.setSelectedIndex(0);
        tbl_pengguna.clearSelection();
        selectedUserId = -1;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelSidebar = new javax.swing.JPanel();
        jLabelLogo = new javax.swing.JLabel();
        jLabelSubLogo = new javax.swing.JLabel();
        btnMenuDashboard = new javax.swing.JButton();
        btnMenuKelolaMenu = new javax.swing.JButton();
        btnMenuKelolaStok = new javax.swing.JButton();
        btnMenuKelolaPengguna = new javax.swing.JButton();
        btnMenuTransaksi = new javax.swing.JButton();
        jPanelMain = new javax.swing.JPanel();
        jPanelHeader = new javax.swing.JPanel();
        jPanelSearch = new javax.swing.JPanel();
        txtSearchHeader = new javax.swing.JTextField();
        jPanelProfile = new javax.swing.JPanel();
        lblProfile = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        jPanelContentWrapper = new javax.swing.JPanel();
        jPanelContent = new javax.swing.JPanel();
        lblDashTitle = new javax.swing.JLabel();
        lblDashSub = new javax.swing.JLabel();
        jPanelForm = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txt_username = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txt_password = new javax.swing.JPasswordField();
        lblRole = new javax.swing.JLabel();
        cb_role = new javax.swing.JComboBox<>();
        btn_tambahUser = new javax.swing.JButton();
        btn_editUser = new javax.swing.JButton();
        btn_hapusUser = new javax.swing.JButton();
        jPanelTable = new javax.swing.JPanel();
        lblTableTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_pengguna = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Kelola Pengguna");

        // --- SIDEBAR ---
        jPanelSidebar.setBackground(new java.awt.Color(139, 29, 36));
        jPanelSidebar.setPreferredSize(new java.awt.Dimension(250, 700));
        jPanelSidebar.setLayout(null);

        jLabelLogo.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabelLogo.setForeground(new java.awt.Color(255, 255, 255));
        jLabelLogo.setText("DoBarPOS");
        jPanelSidebar.add(jLabelLogo);
        jLabelLogo.setBounds(20, 30, 126, 32);

        jLabelSubLogo.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabelSubLogo.setForeground(new java.awt.Color(220, 220, 220));
        jLabelSubLogo.setText("Sistem Manajemen");
        jPanelSidebar.add(jLabelSubLogo);
        jLabelSubLogo.setBounds(20, 60, 116, 16);

        btnMenuDashboard.setBackground(new java.awt.Color(139, 29, 36));
        btnMenuDashboard.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuDashboard.setText("⊞  Dashboard");
        btnMenuDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jPanelSidebar.add(btnMenuDashboard);
        btnMenuDashboard.setBounds(10, 100, 230, 40);

        btnMenuKelolaMenu.setBackground(new java.awt.Color(139, 29, 36));
        btnMenuKelolaMenu.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuKelolaMenu.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuKelolaMenu.setText("🍴  Kelola Menu");
        btnMenuKelolaMenu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jPanelSidebar.add(btnMenuKelolaMenu);
        btnMenuKelolaMenu.setBounds(10, 150, 230, 40);

        btnMenuKelolaStok.setBackground(new java.awt.Color(139, 29, 36));
        btnMenuKelolaStok.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuKelolaStok.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuKelolaStok.setText("📦  Kelola Stok");
        btnMenuKelolaStok.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jPanelSidebar.add(btnMenuKelolaStok);
        btnMenuKelolaStok.setBounds(10, 200, 230, 40);

        btnMenuKelolaPengguna.setBackground(new java.awt.Color(255, 255, 255));
        btnMenuKelolaPengguna.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuKelolaPengguna.setForeground(new java.awt.Color(139, 29, 36));
        btnMenuKelolaPengguna.setText("👥  Kelola Pengguna");
        btnMenuKelolaPengguna.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jPanelSidebar.add(btnMenuKelolaPengguna);
        btnMenuKelolaPengguna.setBounds(10, 250, 230, 40);

        btnMenuTransaksi.setBackground(new java.awt.Color(139, 29, 36));
        btnMenuTransaksi.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuTransaksi.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuTransaksi.setText("📝  Transaksi");
        btnMenuTransaksi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jPanelSidebar.add(btnMenuTransaksi);
        btnMenuTransaksi.setBounds(10, 300, 230, 40);

        getContentPane().add(jPanelSidebar, java.awt.BorderLayout.WEST);

        // --- MAIN PANEL ---
        jPanelMain.setBackground(new java.awt.Color(252, 246, 246));
        jPanelMain.setLayout(new java.awt.BorderLayout());

        // Header
        jPanelHeader.setBackground(new java.awt.Color(255, 255, 255));
        jPanelHeader.setPreferredSize(new java.awt.Dimension(1000, 70));
        jPanelHeader.setLayout(new java.awt.BorderLayout());

        jPanelSearch.setOpaque(false);
        jPanelSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 15));
        txtSearchHeader.setText("Cari staf...");
        txtSearchHeader.setPreferredSize(new java.awt.Dimension(300, 40));
        jPanelSearch.add(txtSearchHeader);
        jPanelHeader.add(jPanelSearch, java.awt.BorderLayout.WEST);

        jPanelProfile.setOpaque(false);
        jPanelProfile.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 20, 15));
        lblProfile.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblProfile.setText("Yaya - Admin");
        jPanelProfile.add(lblProfile);
        btnLogout.setText("Logout");
        btnLogout.setPreferredSize(new java.awt.Dimension(90, 40));
        jPanelProfile.add(btnLogout);
        jPanelHeader.add(jPanelProfile, java.awt.BorderLayout.EAST);

        jPanelMain.add(jPanelHeader, java.awt.BorderLayout.NORTH);

        // Content Wrapper (GridBagLayout agar selalu di tengah)
        jPanelContentWrapper.setBackground(new java.awt.Color(252, 246, 246));
        jPanelContentWrapper.setLayout(new java.awt.GridBagLayout());

        jPanelContent.setBackground(new java.awt.Color(252, 246, 246));
        jPanelContent.setMinimumSize(new java.awt.Dimension(950, 630));
        jPanelContent.setPreferredSize(new java.awt.Dimension(950, 630));
        jPanelContent.setLayout(null);

        // Judul Halaman
        lblDashTitle.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblDashTitle.setText("Kelola Pengguna");
        jPanelContent.add(lblDashTitle);
        lblDashTitle.setBounds(30, 20, 220, 37);

        lblDashSub.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblDashSub.setForeground(new java.awt.Color(138, 138, 138));
        lblDashSub.setText("Tambah, modifikasi, atau hapus operator dan admin sistem.");
        jPanelContent.add(lblDashSub);
        lblDashSub.setBounds(30, 60, 390, 19);

        // --- PANEL KIRI: USER DETAILS FORM ---
        jPanelForm.setBackground(new java.awt.Color(255, 255, 255));
        jPanelForm.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelForm.setLayout(null);

        lblFormTitle.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblFormTitle.setText("👤 Detail Pengguna");
        jPanelForm.add(lblFormTitle);
        lblFormTitle.setBounds(20, 20, 180, 24);

        // USERNAME
        lblUsername.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblUsername.setForeground(new java.awt.Color(80, 80, 80));
        lblUsername.setText("Username");
        jPanelForm.add(lblUsername);
        lblUsername.setBounds(20, 70, 60, 16);

        txt_username.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        txt_username.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        jPanelForm.add(txt_username);
        txt_username.setBounds(20, 90, 250, 38);

        // PASSWORD
        lblPassword.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(80, 80, 80));
        lblPassword.setText("Password");
        jPanelForm.add(lblPassword);
        lblPassword.setBounds(20, 145, 60, 16);

        txt_password.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        txt_password.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        jPanelForm.add(txt_password);
        txt_password.setBounds(20, 165, 250, 38);

        // ROLE (ComboBox sesuai ENUM MySQL: Manager, Admin, Kasir)
        lblRole.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblRole.setForeground(new java.awt.Color(80, 80, 80));
        lblRole.setText("Peran");
        jPanelForm.add(lblRole);
        lblRole.setBounds(20, 220, 40, 16);

        cb_role.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "-- Pilih tingkat akses --", "Manager", "Admin", "Kasir"
        }));
        cb_role.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        jPanelForm.add(cb_role);
        cb_role.setBounds(20, 240, 250, 38);

        // TOMBOL TAMBAH USER (Primary - Merah Tua)
        btn_tambahUser.setBackground(new java.awt.Color(139, 29, 36));
        btn_tambahUser.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_tambahUser.setForeground(new java.awt.Color(255, 255, 255));
        btn_tambahUser.setText("⊕ Tambah User");
        btn_tambahUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_tambahUserActionPerformed(evt);
            }
        });
        jPanelForm.add(btn_tambahUser);
        btn_tambahUser.setBounds(20, 310, 250, 42);

        // TOMBOL EDIT USER (Secondary - Outline)
        btn_editUser.setBackground(new java.awt.Color(255, 255, 255));
        btn_editUser.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btn_editUser.setForeground(new java.awt.Color(80, 80, 80));
        btn_editUser.setText("✎ Edit User");
        btn_editUser.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        btn_editUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_editUserActionPerformed(evt);
            }
        });
        jPanelForm.add(btn_editUser);
        btn_editUser.setBounds(20, 365, 118, 38);

        // TOMBOL HAPUS USER (Danger - Merah Teks)
        btn_hapusUser.setBackground(new java.awt.Color(255, 255, 255));
        btn_hapusUser.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btn_hapusUser.setForeground(new java.awt.Color(139, 29, 36));
        btn_hapusUser.setText("🗑 Hapus User");
        btn_hapusUser.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 200, 200)));
        btn_hapusUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_hapusUserActionPerformed(evt);
            }
        });
        jPanelForm.add(btn_hapusUser);
        btn_hapusUser.setBounds(152, 365, 118, 38);

        jPanelContent.add(jPanelForm);
        jPanelForm.setBounds(30, 110, 300, 430);

        // --- PANEL KANAN: REGISTERED PERSONNEL TABLE ---
        jPanelTable.setBackground(new java.awt.Color(255, 255, 255));
        jPanelTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelTable.setLayout(null);

        lblTableTitle.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblTableTitle.setText("👥 Pengguna Terdaftar");
        jPanelTable.add(lblTableTitle);
        lblTableTitle.setBounds(20, 20, 260, 24);

        // JTable dengan kolom sesuai skema DB: id_user, username, role
        // (Kolom 'Last Login' TIDAK disertakan karena tidak ada di skema MySQL)
        tbl_pengguna.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"1", "admin_super",        "Admin"},
                {"2", "manajer_ops",        "Manager"},
                {"3", "kasir_depan1",       "Kasir"},
                {"4", "kasir_depan2",       "Kasir"},
                {"5", "manajer_shift_malam","Manager"}
            },
            new String [] { "ID USER", "USERNAME", "PERAN" }
        ) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false; // Semua kolom tidak bisa diedit langsung di tabel
            }
        });
        tbl_pengguna.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_penggunaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_pengguna);

        jPanelTable.add(jScrollPane1);
        jScrollPane1.setBounds(20, 60, 580, 355);

        jPanelContent.add(jPanelTable);
        jPanelTable.setBounds(350, 110, 620, 430);

        jPanelContentWrapper.add(jPanelContent, new java.awt.GridBagConstraints());
        jPanelMain.add(jPanelContentWrapper, java.awt.BorderLayout.CENTER);
        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btn_tambahUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_tambahUserActionPerformed
        String username = txt_username.getText().trim();
        String password = new String(txt_password.getPassword());
        String role     = cb_role.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty() || role.startsWith("--")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Username, Password, dan Role tidak boleh kosong!",
                "Validasi", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        // Password disimpan sebagai SHA2-256 (konsisten dengan seed data & UserSession.login)
        String sql = "INSERT INTO user (username, password, role) VALUES (?, SHA2(?, 256), ?)";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username); ps.setString(2, password); ps.setString(3, role);
            ps.executeUpdate();
            javax.swing.JOptionPane.showMessageDialog(this, "User '" + username + "' berhasil ditambahkan!");
            clearForm(); loadDataPengguna();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                javax.swing.JOptionPane.showMessageDialog(this, "Username sudah digunakan! Pilih username lain.",
                    "Duplikat", javax.swing.JOptionPane.WARNING_MESSAGE);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal tambah user: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btn_tambahUserActionPerformed

    private void tbl_penggunaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_penggunaMouseClicked
        int row = tbl_pengguna.getSelectedRow();
        if (row != -1) {
            selectedUserId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            txt_username.setText(tableModel.getValueAt(row, 1).toString());
            cb_role.setSelectedItem(tableModel.getValueAt(row, 2).toString());
            txt_password.setText(""); // Password dikosongkan demi keamanan — isi jika ingin ganti
        }
    }//GEN-LAST:event_tbl_penggunaMouseClicked

    private void btn_editUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_editUserActionPerformed
        if (selectedUserId == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Pilih pengguna dari tabel terlebih dahulu!",
                "Perhatian", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        
        int konfirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menyimpan perubahan pada pengguna ini?", "Konfirmasi Edit",
            javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
            
        if (konfirm == javax.swing.JOptionPane.YES_OPTION) {
            String username = txt_username.getText().trim();
            String role     = cb_role.getSelectedItem().toString();
            String password = new String(txt_password.getPassword());

            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps;
                if (!password.isEmpty()) {
                    // Jika password diisi → update username + password + role
                    ps = conn.prepareStatement(
                        "UPDATE user SET username=?, password=SHA2(?,256), role=? WHERE id_user=?");
                    ps.setString(1, username); ps.setString(2, password);
                    ps.setString(3, role);     ps.setInt(4, selectedUserId);
                } else {
                    // Jika password kosong → update username + role saja (password lama dipertahankan)
                    ps = conn.prepareStatement(
                        "UPDATE user SET username=?, role=? WHERE id_user=?");
                    ps.setString(1, username); ps.setString(2, role); ps.setInt(3, selectedUserId);
                }
                ps.executeUpdate();
                javax.swing.JOptionPane.showMessageDialog(this, "User berhasil diupdate!");
                clearForm(); loadDataPengguna();
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal update user: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btn_editUserActionPerformed

    private void btn_hapusUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_hapusUserActionPerformed
        if (selectedUserId == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Pilih pengguna dari tabel terlebih dahulu!",
                "Perhatian", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        // Cegah menghapus akun sendiri
        if (selectedUserId == UserSession.getInstance().getIdUser()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Tidak dapat menghapus akun yang sedang digunakan!",
                "Ditolak", javax.swing.JOptionPane.ERROR_MESSAGE); return;
        }
        int k = javax.swing.JOptionPane.showConfirmDialog(this,
            "Hapus pengguna ini secara permanen?", "Konfirmasi Hapus",
            javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
        if (k == javax.swing.JOptionPane.YES_OPTION) {
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id_user=?");
                ps.setInt(1, selectedUserId);
                ps.executeUpdate();
                javax.swing.JOptionPane.showMessageDialog(this, "User berhasil dihapus!");
                clearForm(); loadDataPengguna();
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal hapus user: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btn_hapusUserActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new KelolaPenggunaFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnMenuDashboard;
    private javax.swing.JButton btnMenuKelolaMenu;
    private javax.swing.JButton btnMenuKelolaPengguna;
    private javax.swing.JButton btnMenuKelolaStok;
    private javax.swing.JButton btnMenuTransaksi;
    private javax.swing.JButton btn_editUser;
    private javax.swing.JButton btn_hapusUser;
    private javax.swing.JButton btn_tambahUser;
    private javax.swing.JComboBox<String> cb_role;
    private javax.swing.JLabel jLabelLogo;
    private javax.swing.JLabel jLabelSubLogo;
    private javax.swing.JPanel jPanelContent;
    private javax.swing.JPanel jPanelContentWrapper;
    private javax.swing.JPanel jPanelForm;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelProfile;
    private javax.swing.JPanel jPanelSearch;
    private javax.swing.JPanel jPanelSidebar;
    private javax.swing.JPanel jPanelTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDashSub;
    private javax.swing.JLabel lblDashTitle;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblProfile;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblTableTitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JTable tbl_pengguna;
    private javax.swing.JTextField txt_username;
    private javax.swing.JPasswordField txt_password;
    private javax.swing.JTextField txtSearchHeader;
    // End of variables declaration//GEN-END:variables
}
