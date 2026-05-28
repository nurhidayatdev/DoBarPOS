package dobarpos;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

public class KelolaMenuFrame extends javax.swing.JFrame {

    private int selectedMenuId = -1; // ID menu yang sedang dipilih dari tabel
    private DefaultTableModel tableModel;

    public KelolaMenuFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        // Styling tabel
        tbl_menu.setRowHeight(35);
        tbl_menu.setShowGrid(false);
        tbl_menu.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tbl_menu.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        tbl_menu.getTableHeader().setOpaque(false);
        tbl_menu.getTableHeader().setBackground(new Color(252, 246, 246));
        tbl_menu.setSelectionBackground(new Color(252, 225, 225));

        // Highlight menu aktif di sidebar
        btnMenuKelolaMenu.setBackground(Color.WHITE);
        btnMenuKelolaMenu.setForeground(new Color(139, 29, 36));

        // Tampilkan nama user dari sesi yang login
        lblProfile.setText(UserSession.getInstance().getUsername() + " - " + UserSession.getInstance().getRole());

        // Set DefaultTableModel agar bisa diupdate secara dinamis
        tableModel = new DefaultTableModel(
            new String[]{"NO.", "NAMA MENU", "KATEGORI", "HARGA", "STATUS", "_ID"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_menu.setModel(tableModel);

        // Sembunyikan kolom _ID (indeks 5) dari tampilan
        tbl_menu.getColumnModel().getColumn(5).setMinWidth(0);
        tbl_menu.getColumnModel().getColumn(5).setMaxWidth(0);
        tbl_menu.getColumnModel().getColumn(5).setWidth(0);
        // Atur lebar kolom NO. agar ringkas
        tbl_menu.getColumnModel().getColumn(0).setPreferredWidth(50);
        tbl_menu.getColumnModel().getColumn(0).setMaxWidth(60);

        loadDataMenu("");

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

        // RBAC Sidebar (Admin: Dashboard, KelolaMenu, KelolaStok, KelolaPengguna)
        Map<String, JButton> sidebarMap = new LinkedHashMap<>();
        sidebarMap.put("Dashboard",     btnMenuDashboard);
        sidebarMap.put("KelolaMenu",    btnMenuKelolaMenu);
        sidebarMap.put("KelolaStok",    btnMenuKelolaStok);
        sidebarMap.put("KelolaPengguna",btnMenuKelolaPengguna);
        sidebarMap.put("Transaksi",     btnMenuTransaksi);
        sidebarMap.put("Riwayat",       btnMenuRiwayat);
        sidebarMap.put("Laporan",       btnMenuLaporan);
        NavigationHelper.configureSidebar(sidebarMap);
        NavigationHelper.setActiveButton(btnMenuKelolaMenu,
            btnMenuDashboard, btnMenuKelolaMenu, btnMenuKelolaStok,
            btnMenuKelolaPengguna, btnMenuTransaksi, btnMenuRiwayat, btnMenuLaporan);

        // Navigasi
        btnMenuDashboard.addActionListener(e -> NavigationHelper.navigateTo(this, "Dashboard"));
        btnMenuKelolaMenu.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaMenu"));
        btnMenuKelolaStok.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaStok"));
        btnMenuKelolaPengguna.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaPengguna"));
        btnMenuTransaksi.addActionListener(e -> NavigationHelper.navigateTo(this, "Transaksi"));
        btnMenuRiwayat.addActionListener(e -> NavigationHelper.navigateTo(this, "Riwayat"));
        btnMenuLaporan.addActionListener(e -> NavigationHelper.navigateTo(this, "Laporan"));
        btnLogout.addActionListener(e -> NavigationHelper.logout(this));

        // ── Standarisasi Navbar menggunakan NavbarHelper ──────────────────
        NavbarHelper.setupFullNavbar(jPanelHeader, txt_search, lblProfile, btnLogout, "Cari menu (Kopi, Makanan...)");

        // Live search dengan DocumentListener
        txt_search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            private void doSearch() {
                String kw = txt_search.getText();
                if (kw.equals("Cari menu (Kopi, Makanan...)") || kw.trim().isEmpty()) kw = "";
                loadDataMenu(kw);
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
                int formW = 300;
                int tableW = W - pad * 2 - formW - 20;
                lblDashTitle.setBounds(pad, 20, W - pad * 2, 37);
                lblDashSub.setBounds(pad, 60, W - pad * 2, 19);
                jPanelForm.setBounds(pad, 110, formW, 480);
                jPanelTable.setBounds(pad + formW + 20, 110, tableW, 480);
                // Komponen dalam tabel panel
                int scrollW = tableW - 40;
                // txt_search sudah ada di header, tidak perlu diatur bounds di sini
                jScrollPane1.setBounds(20, 60, scrollW, 400);
            }
        });
    }

    /** Membaca data menu dari MySQL dan mengisi tabel */
    private void loadDataMenu(String keyword) {
        tableModel.setRowCount(0); // Kosongkan tabel lebih dulu
        String sql = "SELECT id_menu, nama, kategori, harga, is_available FROM menu "
                   + "WHERE nama LIKE ? ORDER BY kategori, nama";
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            int no = 1;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    no++,
                    rs.getString("nama"),
                    rs.getString("kategori"),
                    rs.getDouble("harga"),
                    rs.getInt("is_available") == 1 ? "Tersedia" : "Habis",
                    rs.getInt("id_menu") // tersembunyi, dipakai untuk edit/hapus
                });
            }
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal load data menu: " + e.getMessage());
        }
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
        jPanelProfile = new javax.swing.JPanel();
        lblProfile = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        jPanelContentWrapper = new javax.swing.JPanel();
        jPanelContent = new javax.swing.JPanel();
        lblDashTitle = new javax.swing.JLabel();
        lblDashSub = new javax.swing.JLabel();
        jPanelForm = new javax.swing.JPanel();
        lblNama = new javax.swing.JLabel();
        txt_namaMenu = new javax.swing.JTextField();
        lblKategori = new javax.swing.JLabel();
        cb_kategori = new javax.swing.JComboBox<>();
        lblHarga = new javax.swing.JLabel();
        txt_harga = new javax.swing.JTextField();
        cb_isAvailable = new javax.swing.JCheckBox();
        btn_tambah = new javax.swing.JButton();
        btn_edit = new javax.swing.JButton();
        btn_hapus = new javax.swing.JButton();
        btn_clear = new javax.swing.JButton();
        jPanelTable = new javax.swing.JPanel();
        txt_search = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_menu = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Kelola Menu");

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

        btnMenuKelolaMenu.setBackground(new java.awt.Color(255, 255, 255));
        btnMenuKelolaMenu.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuKelolaMenu.setForeground(new java.awt.Color(139, 29, 36));
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

        btnMenuKelolaPengguna.setBackground(new java.awt.Color(139, 29, 36));
        btnMenuKelolaPengguna.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuKelolaPengguna.setForeground(new java.awt.Color(255, 255, 255));
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

        jPanelMain.setBackground(new java.awt.Color(252, 246, 246));
        jPanelMain.setLayout(new java.awt.BorderLayout());

        jPanelHeader.setBackground(new java.awt.Color(255, 255, 255));
        jPanelHeader.setPreferredSize(new java.awt.Dimension(1000, 70));
        jPanelHeader.setLayout(new java.awt.BorderLayout());

        jPanelProfile.setOpaque(false);
        jPanelProfile.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 20, 15));

        lblProfile.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblProfile.setText("Yaya - Admin");
        jPanelProfile.add(lblProfile);

        btnLogout.setText("Logout");
        btnLogout.setPreferredSize(new java.awt.Dimension(90, 40));
        jPanelProfile.add(btnLogout);

        // ── Search di Header (kiri) ─────────────────────────────
        javax.swing.JPanel jPanelSearch = new javax.swing.JPanel();
        jPanelSearch.setOpaque(false);
        jPanelSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 15));
        txt_search.setPreferredSize(new java.awt.Dimension(300, 40));
        txt_search.setText("Cari menu...");
        txt_search.setForeground(new java.awt.Color(180, 180, 180));
        txt_search.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        txt_search.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txt_search.getText().equals("Cari menu...")) {
                    txt_search.setText("");
                    txt_search.setForeground(new java.awt.Color(30, 30, 30));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txt_search.getText().isEmpty()) {
                    txt_search.setForeground(new java.awt.Color(180, 180, 180));
                    txt_search.setText("Cari menu...");
                }
            }
        });
        jPanelSearch.add(txt_search);
        jPanelHeader.add(jPanelProfile, java.awt.BorderLayout.EAST);
        jPanelHeader.add(jPanelSearch, java.awt.BorderLayout.WEST);

        jPanelMain.add(jPanelHeader, java.awt.BorderLayout.NORTH);

        jPanelContentWrapper.setBackground(new java.awt.Color(252, 246, 246));
        jPanelContentWrapper.setLayout(new java.awt.GridBagLayout());

        jPanelContent.setBackground(new java.awt.Color(252, 246, 246));
        jPanelContent.setMinimumSize(new java.awt.Dimension(950, 630));
        jPanelContent.setPreferredSize(new java.awt.Dimension(950, 630));
        jPanelContent.setLayout(null);

        lblDashTitle.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblDashTitle.setText("Kelola Menu");
        jPanelContent.add(lblDashTitle);
        lblDashTitle.setBounds(30, 20, 169, 37);

        lblDashSub.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblDashSub.setForeground(new java.awt.Color(138, 138, 138));
        lblDashSub.setText("Tambah, perbarui, atau hapus item dari sistem POS Anda.");
        jPanelContent.add(lblDashSub);
        lblDashSub.setBounds(30, 60, 335, 19);

        jPanelForm.setBackground(new java.awt.Color(255, 255, 255));
        jPanelForm.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelForm.setLayout(null);

        javax.swing.JLabel lblFormTitle = new javax.swing.JLabel();
        lblFormTitle.setFont(new java.awt.Font("SansSerif", 1, 18));
        lblFormTitle.setText("📄 Form Detail Menu");
        jPanelForm.add(lblFormTitle);
        lblFormTitle.setBounds(20, 20, 260, 24);

        lblNama.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblNama.setText("Nama Menu");
        jPanelForm.add(lblNama);
        lblNama.setBounds(20, 70, 67, 16);
        txt_namaMenu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        jPanelForm.add(txt_namaMenu);
        txt_namaMenu.setBounds(20, 90, 250, 35);

        lblKategori.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblKategori.setText("Kategori");
        jPanelForm.add(lblKategori);
        lblKategori.setBounds(20, 140, 48, 16);

        cb_kategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Makanan", "Minuman", "Snack", "Kopi", "Non-Kopi" }));
        cb_kategori.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        jPanelForm.add(cb_kategori);
        cb_kategori.setBounds(20, 160, 250, 35);

        lblHarga.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblHarga.setText("Harga (Rp)");
        jPanelForm.add(lblHarga);
        lblHarga.setBounds(20, 210, 61, 16);

        txt_harga.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_hargaKeyTyped(evt);
            }
        });
        txt_harga.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        jPanelForm.add(txt_harga);
        txt_harga.setBounds(20, 230, 250, 35);

        cb_isAvailable.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cb_isAvailable.setSelected(true);
        cb_isAvailable.setText("Tersedia");
        cb_isAvailable.setOpaque(false);
        jPanelForm.add(cb_isAvailable);
        cb_isAvailable.setBounds(20, 280, 250, 20);

        btn_tambah.setBackground(new java.awt.Color(139, 29, 36));
        btn_tambah.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_tambah.setForeground(new java.awt.Color(255, 255, 255));
        btn_tambah.setText("+ Tambah");
        btn_tambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_tambahActionPerformed(evt);
            }
        });
        jPanelForm.add(btn_tambah);
        btn_tambah.setBounds(20, 330, 250, 40);

        btn_edit.setBackground(new java.awt.Color(252, 209, 225));
        btn_edit.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_edit.setForeground(new java.awt.Color(139, 29, 36));
        btn_edit.setText("Edit");
        btn_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_editActionPerformed(evt);
            }
        });
        jPanelForm.add(btn_edit);
        btn_edit.setBounds(20, 380, 120, 40);

        btn_hapus.setBackground(new java.awt.Color(252, 209, 225));
        btn_hapus.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_hapus.setForeground(new java.awt.Color(139, 29, 36));
        btn_hapus.setText("Hapus");
        btn_hapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_hapusActionPerformed(evt);
            }
        });
        jPanelForm.add(btn_hapus);
        btn_hapus.setBounds(150, 380, 120, 40);

        btn_clear.setBackground(new java.awt.Color(255, 255, 255));
        btn_clear.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_clear.setText("↻ Bersihkan Form");
        btn_clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_clearActionPerformed(evt);
            }
        });
        jPanelForm.add(btn_clear);
        btn_clear.setBounds(20, 430, 250, 40);

        jPanelContent.add(jPanelForm);
        jPanelForm.setBounds(30, 110, 300, 480);

        jPanelTable.setBackground(new java.awt.Color(255, 255, 255));
        jPanelTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelTable.setLayout(null);

        javax.swing.JLabel lblTableTitle = new javax.swing.JLabel();
        lblTableTitle.setFont(new java.awt.Font("SansSerif", 1, 18));
        lblTableTitle.setText("📋 Daftar Menu");
        jPanelTable.add(lblTableTitle);
        lblTableTitle.setBounds(20, 20, 260, 24);

        // txt_search sudah dikelola oleh NavbarHelper di header

        tbl_menu.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"001", "Nasi Goreng Spesial", "Makanan", "35000", "Tersedia"},
                {"002", "Es Teh Manis", "Minuman", "8000", "Tersedia"},
                {"003", "Ayam Bakar Madu", "Makanan", "42000", "Habis"}
            },
            new String [] {
                "ID", "NAMA MENU", "KATEGORI", "HARGA", "STATUS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl_menu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_menuMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_menu);

        jPanelTable.add(jScrollPane1);
        jScrollPane1.setBounds(20, 80, 530, 380);

        jPanelContent.add(jPanelTable);
        jPanelTable.setBounds(350, 110, 570, 480);

        jPanelContentWrapper.add(jPanelContent, new java.awt.GridBagConstraints());

        jPanelMain.add(jPanelContentWrapper, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txt_hargaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_hargaKeyTyped
        // 2. Mencegah input selain angka pada field Harga
        char c = evt.getKeyChar();
        if (!Character.isDigit(c)) {
            evt.consume(); // Abaikan karakter non-angka
        }
    }//GEN-LAST:event_txt_hargaKeyTyped

    private void btn_tambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_tambahActionPerformed
        String nama     = txt_namaMenu.getText().trim();
        String kategori = cb_kategori.getSelectedItem().toString();
        String hargaStr = txt_harga.getText().trim();
        boolean isAvail = cb_isAvailable.isSelected();

        if (nama.isEmpty() || hargaStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nama dan Harga tidak boleh kosong!",
                "Validasi", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        String sql = "INSERT INTO menu (nama, kategori, harga, is_available) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, kategori);
            ps.setDouble(3, Double.parseDouble(hargaStr));
            ps.setInt(4, isAvail ? 1 : 0);
            ps.executeUpdate();
            javax.swing.JOptionPane.showMessageDialog(this, "Menu '" + nama + "' berhasil ditambahkan!");
            clearForm();
            loadDataMenu("");
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal tambah menu: " + e.getMessage());
        }
    }//GEN-LAST:event_btn_tambahActionPerformed

    private void tbl_menuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_menuMouseClicked
        int row = tbl_menu.getSelectedRow();
        if (row != -1) {
            selectedMenuId = Integer.parseInt(tableModel.getValueAt(row, 5).toString()); // kolom _ID tersembunyi
            txt_namaMenu.setText(tableModel.getValueAt(row, 1).toString());
            cb_kategori.setSelectedItem(tableModel.getValueAt(row, 2).toString());
            txt_harga.setText(tableModel.getValueAt(row, 3).toString());
            cb_isAvailable.setSelected(tableModel.getValueAt(row, 4).toString().equalsIgnoreCase("Tersedia"));
        }
    }//GEN-LAST:event_tbl_menuMouseClicked

    private void btn_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_editActionPerformed
        if (selectedMenuId == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih menu dari tabel terlebih dahulu!",
                "Perhatian", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        
        int konfirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menyimpan perubahan pada menu ini?", "Konfirmasi Edit",
            javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
            
        if (konfirm == javax.swing.JOptionPane.YES_OPTION) {
            String nama     = txt_namaMenu.getText().trim();
            String kategori = cb_kategori.getSelectedItem().toString();
            String hargaStr = txt_harga.getText().trim();
            boolean isAvail = cb_isAvailable.isSelected();

            String sql = "UPDATE menu SET nama=?, kategori=?, harga=?, is_available=? WHERE id_menu=?";
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nama); ps.setString(2, kategori);
                ps.setDouble(3, Double.parseDouble(hargaStr));
                ps.setInt(4, isAvail ? 1 : 0);
                ps.setInt(5, selectedMenuId);
                ps.executeUpdate();
                javax.swing.JOptionPane.showMessageDialog(this, "Menu berhasil diupdate!");
                clearForm();
                loadDataMenu("");
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal update menu: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btn_editActionPerformed

    private void btn_hapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_hapusActionPerformed
        if (selectedMenuId == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih menu dari tabel terlebih dahulu!",
                "Perhatian", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        int konfirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Hapus menu ini secara permanen?", "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
        if (konfirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM menu WHERE id_menu=?");
                ps.setInt(1, selectedMenuId);
                ps.executeUpdate();
                javax.swing.JOptionPane.showMessageDialog(this, "Menu berhasil dihapus!");
                clearForm();
                loadDataMenu("");
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal hapus menu: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btn_hapusActionPerformed

    private void btn_clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_clearActionPerformed
        clearForm();
    }//GEN-LAST:event_btn_clearActionPerformed

    private void clearForm() {
        txt_namaMenu.setText("");
        txt_harga.setText("");
        cb_kategori.setSelectedIndex(0);
        cb_isAvailable.setSelected(true);
        tbl_menu.clearSelection();
        selectedMenuId = -1;
    }

    private void txt_searchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchKeyReleased
        String keyword = txt_search.getText().trim();
        // Abaikan teks placeholder
        if (keyword.equals("Cari...")) keyword = "";
        loadDataMenu(keyword); // Live search: reload tabel setiap ketikan
    }//GEN-LAST:event_txt_searchKeyReleased

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new KelolaMenuFrame().setVisible(true);
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
    private javax.swing.JButton btn_clear;
    private javax.swing.JButton btn_edit;
    private javax.swing.JButton btn_hapus;
    private javax.swing.JButton btn_tambah;
    private javax.swing.JCheckBox cb_isAvailable;
    private javax.swing.JComboBox<String> cb_kategori;
    private javax.swing.JLabel jLabelLogo;
    private javax.swing.JLabel jLabelSubLogo;
    private javax.swing.JPanel jPanelContent;
    private javax.swing.JPanel jPanelContentWrapper;
    private javax.swing.JPanel jPanelForm;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelProfile;
    private javax.swing.JPanel jPanelSidebar;
    private javax.swing.JPanel jPanelTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDashSub;
    private javax.swing.JLabel lblDashTitle;
    private javax.swing.JLabel lblHarga;
    private javax.swing.JLabel lblKategori;
    private javax.swing.JLabel lblNama;
    private javax.swing.JLabel lblProfile;
    private javax.swing.JTable tbl_menu;
    private javax.swing.JTextField txt_harga;
    private javax.swing.JTextField txt_namaMenu;
    private javax.swing.JTextField txt_search;
    // End of variables declaration//GEN-END:variables
}
