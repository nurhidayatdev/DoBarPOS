package dobarpos;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class KelolaStokFrame extends javax.swing.JFrame {

    private int selectedStokId = -1;
    private DefaultTableModel tableModel;

    public KelolaStokFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        tbl_stok.setRowHeight(40);
        tbl_stok.setShowGrid(false);
        tbl_stok.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tbl_stok.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        tbl_stok.getTableHeader().setOpaque(false);
        tbl_stok.getTableHeader().setBackground(new Color(252, 246, 246));
        tbl_stok.setSelectionBackground(new Color(252, 225, 225));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        btnMenuKelolaStok.setBackground(Color.WHITE);
        btnMenuKelolaStok.setForeground(new Color(139, 29, 36));
        lblProfile.setText(UserSession.getInstance().getUsername() + " - " + UserSession.getInstance().getRole());

        tableModel = new DefaultTableModel(
            new String[]{"NO.", "NAMA BAHAN", "JUMLAH", "SATUAN", "UPDATE OLEH", "ID_HIDDEN"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_stok.setModel(tableModel);

        // Sembunyikan kolom ID aslinya
        tbl_stok.getColumnModel().getColumn(5).setMinWidth(0);
        tbl_stok.getColumnModel().getColumn(5).setMaxWidth(0);
        tbl_stok.getColumnModel().getColumn(5).setWidth(0);

        // Center-align kolom NO, JUMLAH
        tbl_stok.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tbl_stok.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // Atur lebar kolom NO agar tidak terlalu besar
        tbl_stok.getColumnModel().getColumn(0).setPreferredWidth(50);
        tbl_stok.getColumnModel().getColumn(0).setMaxWidth(60);

        loadDataStok("");
        
        // Standarisasi Navbar menggunakan NavbarHelper
        NavbarHelper.setupFullNavbar(jPanelHeader, txtSearchHeader, lblProfile, btnLogout, "Cari stok bahan...");

        // Search Logic
        txtSearchHeader.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            private void search() {
                String text = txtSearchHeader.getText();
                if (text.equals("Cari stok bahan...") || text.trim().isEmpty()) {
                    loadDataStok("");
                } else {
                    loadDataStok(text);
                }
            }
        });

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
        NavigationHelper.setActiveButton(btnMenuKelolaStok,
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
                int panelW = W - pad * 2;
                lblDashTitle.setBounds(pad, 20, panelW, 37);
                lblDashSub.setBounds(pad, 60, panelW, 19);
                // Panel Input (form atas)
                jPanelInput.setBounds(pad, 100, panelW, 200); // perbesar tinggi untuk separator
                
                int xNama = 20;
                int xJumlah = (int)(panelW * 0.40);
                int xSatuan = (int)(panelW * 0.65);
                
                lblNamaBahan.setBounds(xNama, 60, (int)(panelW * 0.35), 16);
                txt_namaBahan.setBounds(xNama, 80, (int)(panelW * 0.35), 35);
                
                lblJumlah.setBounds(xJumlah, 60, (int)(panelW * 0.20), 16);
                spin_jumlah.setBounds(xJumlah, 80, (int)(panelW * 0.20), 35);
                
                lblSatuan.setBounds(xSatuan, 60, (int)(panelW * 0.25), 16);
                cb_satuan.setBounds(xSatuan, 80, (int)(panelW * 0.25), 35);
                
                // Tambahkan garis di listener (meskipun lebih baik di initComponents, tapi untuk kemudahan layouting)
                // Kita akan pakai batas y=140
                
                btn_tambahStok.setBounds(20, 145, 140, 38);
                btn_updateStok.setBounds(180, 145, 140, 38);
                btn_hapusStok.setBounds(panelW - 160, 145, 140, 38);
                // Panel Inventaris (tabel bawah)
                jPanelInventaris.setBounds(pad, 320, panelW, 310);
                jScrollPane1.setBounds(20, 60, panelW - 40, 230);
                
                // Jika ada komponen separator, resize juga:
                if (jPanelInput.getClientProperty("sep") != null) {
                    ((javax.swing.JSeparator)jPanelInput.getClientProperty("sep")).setBounds(20, 130, panelW - 40, 10);
                }
            }
        });
    }

    private void loadDataStok(String keyword) {
        tableModel.setRowCount(0);
        String sql = "SELECT s.id_stok, s.nama_bahan, s.jumlah, s.satuan, u.username "
                   + "FROM stok_bahan s LEFT JOIN user u ON s.update_by = u.id_user "
                   + "WHERE s.nama_bahan LIKE ? ORDER BY s.nama_bahan";
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
                    rs.getString("nama_bahan"),
                    rs.getDouble("jumlah"),
                    rs.getString("satuan"),
                    rs.getString("username") != null ? rs.getString("username") : "-",
                    rs.getInt("id_stok")
                });
            }
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal load data stok: " + e.getMessage());
        }
    }


    private void clearForm() {
        txt_namaBahan.setText("");
        spin_jumlah.setValue(0.0);
        cb_satuan.setSelectedIndex(0);
        tbl_stok.clearSelection();
        selectedStokId = -1;
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
        jPanelInput = new javax.swing.JPanel();
        lblInputTitle = new javax.swing.JLabel();
        lblNamaBahan = new javax.swing.JLabel();
        txt_namaBahan = new javax.swing.JTextField();
        lblJumlah = new javax.swing.JLabel();
        spin_jumlah = new javax.swing.JSpinner();
        lblSatuan = new javax.swing.JLabel();
        cb_satuan = new javax.swing.JComboBox<>();
        btn_tambahStok = new javax.swing.JButton();
        btn_updateStok = new javax.swing.JButton();
        btn_hapusStok = new javax.swing.JButton();
        jPanelInventaris = new javax.swing.JPanel();
        lblInventarisTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_stok = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Kelola Stok");

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

        btnMenuKelolaStok.setBackground(new java.awt.Color(255, 255, 255));
        btnMenuKelolaStok.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuKelolaStok.setForeground(new java.awt.Color(139, 29, 36));
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

        jPanelSearch.setOpaque(false);
        jPanelSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 15));

        txtSearchHeader.setText("Cari stok...");
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

        jPanelContentWrapper.setBackground(new java.awt.Color(252, 246, 246));
        jPanelContentWrapper.setLayout(new java.awt.GridBagLayout());

        jPanelContent.setBackground(new java.awt.Color(252, 246, 246));
        jPanelContent.setMinimumSize(new java.awt.Dimension(950, 630));
        jPanelContent.setPreferredSize(new java.awt.Dimension(950, 630));
        jPanelContent.setLayout(null);

        lblDashTitle.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblDashTitle.setText("Kelola Stok");
        jPanelContent.add(lblDashTitle);
        lblDashTitle.setBounds(30, 20, 169, 37);

        lblDashSub.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblDashSub.setForeground(new java.awt.Color(138, 138, 138));
        lblDashSub.setText("Kelola inventaris, perbarui jumlah, dan lacak bahan baku.");
        jPanelContent.add(lblDashSub);
        lblDashSub.setBounds(30, 60, 400, 19);

        jPanelInput.setBackground(new java.awt.Color(255, 255, 255));
        jPanelInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelInput.setLayout(null);

        lblInputTitle.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblInputTitle.setText("Input Data Bahan");
        jPanelInput.add(lblInputTitle);
        lblInputTitle.setBounds(20, 20, 153, 24);

        lblNamaBahan.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblNamaBahan.setForeground(new java.awt.Color(138, 138, 138));
        lblNamaBahan.setText("NAMA BAHAN");
        jPanelInput.add(lblNamaBahan);
        lblNamaBahan.setBounds(20, 60, 100, 16);
        
        txt_namaBahan.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        jPanelInput.add(txt_namaBahan);
        txt_namaBahan.setBounds(20, 80, 300, 35);

        lblJumlah.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblJumlah.setForeground(new java.awt.Color(138, 138, 138));
        lblJumlah.setText("JUMLAH");
        jPanelInput.add(lblJumlah);
        lblJumlah.setBounds(340, 60, 100, 16);

        spin_jumlah.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));
        jPanelInput.add(spin_jumlah);
        spin_jumlah.setBounds(340, 80, 200, 35);

        lblSatuan.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblSatuan.setForeground(new java.awt.Color(138, 138, 138));
        lblSatuan.setText("SATUAN");
        jPanelInput.add(lblSatuan);
        lblSatuan.setBounds(560, 60, 100, 16);

        cb_satuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Kilogram (kg)", "Liter (l)", "Pcs" }));
        jPanelInput.add(cb_satuan);
        cb_satuan.setBounds(560, 80, 200, 35);

        btn_tambahStok.setBackground(new java.awt.Color(139, 29, 36));
        btn_tambahStok.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_tambahStok.setForeground(new java.awt.Color(255, 255, 255));
        btn_tambahStok.setText("+ Tambah Stok");
        btn_tambahStok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_tambahStokActionPerformed(evt);
            }
        });
        jPanelInput.add(btn_tambahStok);
        btn_tambahStok.setBounds(20, 130, 140, 35);

        btn_updateStok.setBackground(new java.awt.Color(255, 255, 255));
        btn_updateStok.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_updateStok.setForeground(new java.awt.Color(139, 29, 36));
        btn_updateStok.setText("✎ Update Stok");
        btn_updateStok.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(139, 29, 36)));
        btn_updateStok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_updateStokActionPerformed(evt);
            }
        });
        jPanelInput.add(btn_updateStok);
        btn_updateStok.setBounds(180, 130, 140, 35);

        btn_hapusStok.setBackground(new java.awt.Color(255, 255, 255));
        btn_hapusStok.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btn_hapusStok.setForeground(new java.awt.Color(139, 29, 36));
        btn_hapusStok.setText("🗑 Hapus Stok");
        btn_hapusStok.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(139, 29, 36)));
        btn_hapusStok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_hapusStokActionPerformed(evt);
            }
        });
        jPanelInput.add(btn_hapusStok);
        btn_hapusStok.setBounds(730, 130, 140, 35);

        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        jPanelInput.putClientProperty("sep", sep);
        jPanelInput.add(sep);

        jPanelContent.add(jPanelInput);
        jPanelInput.setBounds(30, 100, 890, 200);

        jPanelInventaris.setBackground(new java.awt.Color(255, 255, 255));
        jPanelInventaris.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelInventaris.setLayout(null);

        lblInventarisTitle.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblInventarisTitle.setText("Data Inventaris");
        jPanelInventaris.add(lblInventarisTitle);
        lblInventarisTitle.setBounds(20, 20, 250, 24);

        tbl_stok.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"1", "Biji Kopi Arabica", "25.5", "kg", ""},
                {"2", "Susu Segar (Full Cream)", "40.0", "liter", ""},
                {"3", "Gula Aren Cair", "15.0", "liter", ""},
                {"4", "Cup Plastik 16oz", "500", "pcs", ""}
            },
            new String [] {
                "NO", "NAMA BAHAN", "JUMLAH", "SATUAN", "AKSI"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl_stok.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_stokMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_stok);

        jPanelInventaris.add(jScrollPane1);
        jScrollPane1.setBounds(20, 60, 850, 230);

        jPanelContent.add(jPanelInventaris);
        jPanelInventaris.setBounds(30, 300, 890, 310);

        jPanelContentWrapper.add(jPanelContent, new java.awt.GridBagConstraints());

        jPanelMain.add(jPanelContentWrapper, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btn_tambahStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_tambahStokActionPerformed
        String nama   = txt_namaBahan.getText().trim();
        double jumlah = (Double) spin_jumlah.getValue();
        String satuan = cb_satuan.getSelectedItem().toString();
        int idUser    = UserSession.getInstance().getIdUser();

        if (nama.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nama bahan tidak boleh kosong!",
                "Validasi", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        String sql = "INSERT INTO stok_bahan (nama_bahan, jumlah, satuan, update_by) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama); ps.setDouble(2, jumlah);
            ps.setString(3, satuan); ps.setInt(4, idUser);
            ps.executeUpdate();
            javax.swing.JOptionPane.showMessageDialog(this, "Stok '" + nama + "' berhasil ditambahkan!");
            clearForm(); loadDataStok("");
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal tambah stok: " + e.getMessage());
        }
    }//GEN-LAST:event_btn_tambahStokActionPerformed

    private void tbl_stokMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_stokMouseClicked
        int row = tbl_stok.getSelectedRow();
        if (row != -1) {
            selectedStokId = Integer.parseInt(tableModel.getValueAt(row, 5).toString());
            String nama   = tableModel.getValueAt(row, 1).toString();
            String jumlah = tableModel.getValueAt(row, 2).toString();
            String satuan = tableModel.getValueAt(row, 3).toString();

            txt_namaBahan.setText(nama);
            spin_jumlah.setValue(Double.parseDouble(jumlah));
            for (int i = 0; i < cb_satuan.getItemCount(); i++) {
                if (cb_satuan.getItemAt(i).toLowerCase().contains(satuan.toLowerCase().replace(" (kg)","").replace(" (l)",""))) {
                    cb_satuan.setSelectedIndex(i); break;
                }
            }
        }
    }//GEN-LAST:event_tbl_stokMouseClicked

    private void btn_updateStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_updateStokActionPerformed
        if (selectedStokId == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih bahan dari tabel terlebih dahulu!",
                "Perhatian", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        
        int konfirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menyimpan perubahan pada stok ini?", "Konfirmasi Edit",
            javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
            
        if (konfirm == javax.swing.JOptionPane.YES_OPTION) {
            String nama   = txt_namaBahan.getText().trim();
            double jumlah = (Double) spin_jumlah.getValue();
            String satuan = cb_satuan.getSelectedItem().toString();
            int idUser    = UserSession.getInstance().getIdUser();

            String sql = "UPDATE stok_bahan SET nama_bahan=?, jumlah=?, satuan=?, update_by=? WHERE id_stok=?";
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nama); ps.setDouble(2, jumlah);
                ps.setString(3, satuan); ps.setInt(4, idUser); ps.setInt(5, selectedStokId);
                ps.executeUpdate();
                javax.swing.JOptionPane.showMessageDialog(this, "Stok berhasil diupdate!");
                clearForm(); loadDataStok("");
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal update stok: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btn_updateStokActionPerformed

    private void btn_hapusStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_hapusStokActionPerformed
        if (selectedStokId == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih bahan dari tabel terlebih dahulu!",
                "Perhatian", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        int k = javax.swing.JOptionPane.showConfirmDialog(this,
            "Hapus bahan stok ini?", "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
        if (k == javax.swing.JOptionPane.YES_OPTION) {
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM stok_bahan WHERE id_stok=?");
                ps.setInt(1, selectedStokId);
                ps.executeUpdate();
                javax.swing.JOptionPane.showMessageDialog(this, "Stok berhasil dihapus!");
                clearForm(); loadDataStok("");
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal hapus stok: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btn_hapusStokActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new KelolaStokFrame().setVisible(true);
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
    private javax.swing.JButton btn_hapusStok;
    private javax.swing.JButton btn_tambahStok;
    private javax.swing.JButton btn_updateStok;
    private javax.swing.JComboBox<String> cb_satuan;
    private javax.swing.JLabel jLabelLogo;
    private javax.swing.JLabel jLabelSubLogo;
    private javax.swing.JPanel jPanelContent;
    private javax.swing.JPanel jPanelContentWrapper;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelInput;
    private javax.swing.JPanel jPanelInventaris;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelProfile;
    private javax.swing.JPanel jPanelSearch;
    private javax.swing.JPanel jPanelSidebar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDashSub;
    private javax.swing.JLabel lblDashTitle;
    private javax.swing.JLabel lblInputTitle;
    private javax.swing.JLabel lblInventarisTitle;
    private javax.swing.JLabel lblJumlah;
    private javax.swing.JLabel lblNamaBahan;
    private javax.swing.JLabel lblProfile;
    private javax.swing.JLabel lblSatuan;
    private javax.swing.JSpinner spin_jumlah;
    private javax.swing.JTable tbl_stok;
    private javax.swing.JTextField txtSearchHeader;
    private javax.swing.JTextField txt_namaBahan;
    // End of variables declaration//GEN-END:variables
}
