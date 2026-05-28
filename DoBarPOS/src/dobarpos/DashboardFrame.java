package dobarpos;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;

public class DashboardFrame extends javax.swing.JFrame {

    public DashboardFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        // Tambah tombol Riwayat & Laporan ke Sidebar secara dinamis agar seragam
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

        // Konfigurasi sidebar sesuai role (sembunyikan menu yang tidak diizinkan)
        Map<String, JButton> sidebarMap = new LinkedHashMap<>();
        sidebarMap.put("Dashboard",      btnMenuDashboard);
        sidebarMap.put("KelolaMenu",      btnMenuKelolaMenu);
        sidebarMap.put("KelolaStok",      btnMenuKelolaStok);
        sidebarMap.put("KelolaPengguna",  btnMenuKelolaPengguna);
        sidebarMap.put("Transaksi",       btnMenuTransaksi);
        sidebarMap.put("Riwayat",         btnMenuRiwayat);
        sidebarMap.put("Laporan",         btnMenuLaporan);
        NavigationHelper.configureSidebar(sidebarMap);
        NavigationHelper.setActiveButton(btnMenuDashboard,
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

        lblProfile.setText(UserSession.getInstance().getUsername() + " - " + UserSession.getInstance().getRole());
        
        // Standarisasi Navbar menggunakan NavbarHelper
        NavbarHelper.setupFullNavbar(jPanelHeader, txtSearch, lblProfile, btnLogout, "Cari ringkasan data...");

        // Quick Actions
        btnNewOrder.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaMenu"));
        btnRestock.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaStok"));

        // Custom Icons
        lblTitleCard1.setText("💵 PENJUALAN HARI INI");
        lblTitleCard2.setText("⚠️ STOK MENIPIS");
        lblTitleCard3.setText("👥 STAF AKTIF");
        jLabel5.setText("✅ Semua sistem beroperasi normal");
        
        btnNewOrder.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnRestock.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnNewOrder.setText("+ Menu Baru");
        btnRestock.setText("↺  Restock");
        btnNewOrder.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        btnRestock.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        btnNewOrder.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRestock.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);

        // Search Logic
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            private void filterTable() {
                String text = txtSearch.getText();
                javax.swing.table.TableRowSorter<?> sorter = (javax.swing.table.TableRowSorter<?>) jTable1.getRowSorter();
                if (sorter != null) {
                    if (text.equals("Cari ringkasan data...") || text.trim().isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text));
                    }
                }
            }
        });

        // =====================================================================
        // LAYOUT DINAMIS - Isi penuh area konten, sama seperti LaporanFrame
        // =====================================================================
        jPanelContentWrapper.setLayout(new java.awt.BorderLayout());
        jPanelContentWrapper.removeAll();
        jPanelContentWrapper.add(jPanelContent, java.awt.BorderLayout.CENTER);
        jPanelContent.setPreferredSize(null);
        jPanelContent.setMinimumSize(null);

        // ComponentListener: setiap kali panel diubah ukurannya, atur ulang posisi komponen
        jPanelContent.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int W = jPanelContent.getWidth();
                int pad = 30;
                int cardW = (W - pad * 4) / 3;
                int qaW = (int)(W * 0.28);
                int tableW = W - pad * 2 - qaW - 20;

                // Header
                lblDashTitle.setBounds(pad, 20, W - pad * 2, 40);
                lblDashSub.setBounds(pad, 60, W - pad * 2, 20);

                // Cards
                jPanelCard1.setBounds(pad, 100, cardW, 130);
                jPanelCard2.setBounds(pad + cardW + pad, 100, cardW, 130);
                jPanelCard3.setBounds(pad + (cardW + pad) * 2, 100, cardW, 130);

                // Labels di dalam Cards
                lblTitleCard1.setBounds(20, 30, cardW - 30, 25);
                lblValueCard1.setBounds(20, 65, cardW - 30, 45);
                lblTitleCard2.setBounds(20, 30, cardW - 30, 25);
                lblValueCard2.setBounds(20, 65, cardW - 30, 45);
                lblTitleCard3.setBounds(20, 30, cardW - 30, 25);
                lblValueCard3.setBounds(20, 65, cardW - 30, 45);

                // Tabel & Quick Actions
                jScrollPane1.setBounds(pad, 260, tableW, 300);
                int qaX = pad + tableW + 20;
                jPanelQA.setBounds(qaX, 260, qaW, 300);

                // Elemen di dalam QA Panel
                jLabel3.setBounds(20, 20, qaW - 30, 30);
                btnNewOrder.setBounds(10, 70, (qaW - 30) / 2, 90);
                btnRestock.setBounds(20 + (qaW - 30) / 2, 70, (qaW - 30) / 2, 90);
                jLabel4.setBounds(20, 190, qaW - 30, 20);
                jLabel5.setBounds(20, 218, qaW - 30, 20);
            }
        });
        // =====================================================================

        loadDashboardData();
    }

    private void loadDashboardData() {
        try (java.sql.Connection conn = DBConnection.getConnection()) {
            // Card 1: Today's Sales
            String sqlSales = "SELECT COALESCE(SUM(total), 0) as today_sales FROM transaksi WHERE DATE(created_at) = CURDATE() AND status != 'Refund' AND status != 'Void'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlSales);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double sales = rs.getDouble("today_sales");
                    java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
                    lblValueCard1.setText("Rp " + fmt.format(sales));
                }
            }

            // Card 2: Low Stock
            String sqlStock = "SELECT COUNT(*) as low_stock FROM stok_bahan WHERE jumlah <= 10";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlStock);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblValueCard2.setText(String.valueOf(rs.getInt("low_stock")));
                }
            }

            // Card 3: Active Staff
            String sqlStaff = "SELECT COUNT(*) as active_staff FROM user";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlStaff);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblValueCard3.setText(String.valueOf(rs.getInt("active_staff")));
                }
            }

            // Table: Recent Transactions
            String sqlTable = "SELECT t.id_transaksi, DATE_FORMAT(t.created_at, '%H:%i') AS time, "
                            + "COALESCE((SELECT SUM(jumlah) FROM order_detail WHERE id_pemesanan = t.id_pemesanan), 0) AS items, "
                            + "t.total, t.status "
                            + "FROM transaksi t ORDER BY t.created_at DESC LIMIT 5";
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new String[] {"ID Pesanan", "Waktu", "Item", "Total", "Status"}, 0
            ) {
                public boolean isCellEditable(int row, int column) { return false; }
            };
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlTable);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
                while (rs.next()) {
                    model.addRow(new Object[]{
                        "#TRX-" + rs.getInt("id_transaksi"),
                        rs.getString("time"),
                        rs.getInt("items") + " Items",
                        "Rp " + fmt.format(rs.getDouble("total")),
                        rs.getString("status")
                    });
                }
            }
            jTable1.setModel(model);
            
            // Pasang RowSorter untuk fitur Search
            javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
            jTable1.setRowSorter(sorter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

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
        txtSearch = new javax.swing.JTextField();
        jPanelProfile = new javax.swing.JPanel();
        lblProfile = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        jPanelContentWrapper = new javax.swing.JPanel();
        jPanelContent = new javax.swing.JPanel();
        lblDashTitle = new javax.swing.JLabel();
        lblDashSub = new javax.swing.JLabel();
        jPanelCard1 = new javax.swing.JPanel();
        lblTitleCard1 = new javax.swing.JLabel();
        lblValueCard1 = new javax.swing.JLabel();
        jPanelCard2 = new javax.swing.JPanel();
        lblTitleCard2 = new javax.swing.JLabel();
        lblValueCard2 = new javax.swing.JLabel();
        jPanelCard3 = new javax.swing.JPanel();
        lblTitleCard3 = new javax.swing.JLabel();
        lblValueCard3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanelQA = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnNewOrder = new javax.swing.JButton();
        btnRestock = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Dashboard Overview");

        jPanelSidebar.setBackground(new java.awt.Color(139, 29, 36));
        jPanelSidebar.setPreferredSize(new java.awt.Dimension(250, 700));
        jPanelSidebar.setLayout(null);

        jLabelLogo.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabelLogo.setForeground(new java.awt.Color(255, 255, 255));
        jLabelLogo.setText("DoBarPOS");
        jPanelSidebar.add(jLabelLogo);
        jLabelLogo.setBounds(20, 30, 123, 32);

        jLabelSubLogo.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabelSubLogo.setForeground(new java.awt.Color(220, 220, 220));
        jLabelSubLogo.setText("Sistem Manajemen");
        jPanelSidebar.add(jLabelSubLogo);
        jLabelSubLogo.setBounds(20, 60, 112, 16);

        btnMenuDashboard.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMenuDashboard.setForeground(new java.awt.Color(139, 29, 36));
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

        txtSearch.setPreferredSize(new java.awt.Dimension(300, 40));
        txtSearch.setText("Cari data POS...");
        jPanelSearch.add(txtSearch);

        jPanelHeader.add(jPanelSearch, java.awt.BorderLayout.WEST);

        jPanelProfile.setOpaque(false);
        jPanelProfile.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 20, 15));

        lblProfile.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblProfile.setText("Yaya - Manager");
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
        lblDashTitle.setText("Ringkasan Dashboard");
        jPanelContent.add(lblDashTitle);
        lblDashTitle.setBounds(30, 20, 278, 37);

        lblDashSub.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblDashSub.setForeground(new java.awt.Color(138, 138, 138));
        lblDashSub.setText("Metrik waktu nyata dan status sistem.");
        jPanelContent.add(lblDashSub);
        lblDashSub.setBounds(30, 60, 230, 19);

        jPanelCard1.setBackground(new java.awt.Color(255, 255, 255));
        jPanelCard1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelCard1.setLayout(null);

        lblTitleCard1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTitleCard1.setForeground(new java.awt.Color(138, 138, 138));
        lblTitleCard1.setText("PENJUALAN HARI INI");
        jPanelCard1.add(lblTitleCard1);
        lblTitleCard1.setBounds(20, 60, 97, 16);

        lblValueCard1.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblValueCard1.setText("Rp 15.4M");
        jPanelCard1.add(lblValueCard1);
        lblValueCard1.setBounds(20, 80, 123, 37);

        jPanelContent.add(jPanelCard1);
        jPanelCard1.setBounds(30, 100, 250, 130);

        jPanelCard2.setBackground(new java.awt.Color(255, 255, 255));
        jPanelCard2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelCard2.setLayout(null);

        lblTitleCard2.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTitleCard2.setForeground(new java.awt.Color(138, 138, 138));
        lblTitleCard2.setText("STOK MENIPIS");
        jPanelCard2.add(lblTitleCard2);
        lblTitleCard2.setBounds(20, 60, 113, 16);

        lblValueCard2.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblValueCard2.setText("12");
        jPanelCard2.add(lblValueCard2);
        lblValueCard2.setBounds(20, 80, 31, 37);

        jPanelContent.add(jPanelCard2);
        jPanelCard2.setBounds(300, 100, 250, 130);

        jPanelCard3.setBackground(new java.awt.Color(255, 255, 255));
        jPanelCard3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelCard3.setLayout(null);

        lblTitleCard3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTitleCard3.setForeground(new java.awt.Color(138, 138, 138));
        lblTitleCard3.setText("STAF AKTIF");
        jPanelCard3.add(lblTitleCard3);
        lblTitleCard3.setBounds(20, 60, 87, 16);

        lblValueCard3.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblValueCard3.setText("5");
        jPanelCard3.add(lblValueCard3);
        lblValueCard3.setBounds(20, 80, 15, 37);

        jPanelContent.add(jPanelCard3);
        jPanelCard3.setBounds(570, 100, 250, 130);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"#TRX-0092", "10:42 AM", "3 Item", "Rp 125.000", "Selesai"},
                {"#TRX-0091", "10:15 AM", "1 Item", "Rp 45.000", "Selesai"},
                {"#TRX-0090", "09:55 AM", "5 Item", "Rp 310.000", "Tertunda"}
            },
            new String [] {
                "ID Pesanan", "Waktu", "Item", "Total", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanelContent.add(jScrollPane1);
        jScrollPane1.setBounds(30, 260, 520, 300);

        jPanelQA.setBackground(new java.awt.Color(255, 255, 255));
        jPanelQA.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        jPanelQA.setLayout(null);

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel3.setText("Aksi Cepat");
        jPanelQA.add(jLabel3);
        jLabel3.setBounds(20, 20, 123, 24);

        btnNewOrder.setText("Pesanan Baru");
        jPanelQA.add(btnNewOrder);
        btnNewOrder.setBounds(20, 60, 100, 80);

        btnRestock.setText("Isi Stok");
        jPanelQA.add(btnRestock);
        btnRestock.setBounds(130, 60, 100, 80);

        jLabel4.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel4.setText("Status Sistem");
        jPanelQA.add(jLabel4);
        jLabel4.setBounds(20, 170, 97, 19);

        jLabel5.setText("Semua sistem beroperasi normal");
        jPanelQA.add(jLabel5);
        jLabel5.setBounds(20, 200, 123, 16);

        jPanelContent.add(jPanelQA);
        jPanelQA.setBounds(570, 260, 250, 300);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanelContentWrapper.add(jPanelContent, gridBagConstraints);

        jPanelMain.add(jPanelContentWrapper, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashboardFrame().setVisible(true);
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
    private javax.swing.JButton btnNewOrder;
    private javax.swing.JButton btnRestock;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelLogo;
    private javax.swing.JLabel jLabelSubLogo;
    private javax.swing.JPanel jPanelCard1;
    private javax.swing.JPanel jPanelCard2;
    private javax.swing.JPanel jPanelCard3;
    private javax.swing.JPanel jPanelContent;
    private javax.swing.JPanel jPanelContentWrapper;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelProfile;
    private javax.swing.JPanel jPanelQA;
    private javax.swing.JPanel jPanelSearch;
    private javax.swing.JPanel jPanelSidebar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblDashSub;
    private javax.swing.JLabel lblDashTitle;
    private javax.swing.JLabel lblProfile;
    private javax.swing.JLabel lblTitleCard1;
    private javax.swing.JLabel lblTitleCard2;
    private javax.swing.JLabel lblTitleCard3;
    private javax.swing.JLabel lblValueCard1;
    private javax.swing.JLabel lblValueCard2;
    private javax.swing.JLabel lblValueCard3;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
