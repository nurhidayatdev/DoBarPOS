package dobarpos;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RiwayatFrame extends JFrame {

    private DefaultTableModel riwayatModel, detailModel;
    private String filterWaktuAktif = "Hari Ini";
    private String filterStatusAktif = "Semua";
    private String tanggalCustomAktif = null;

    public RiwayatFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadRiwayat();

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
        NavigationHelper.setActiveButton(btnMenuRiwayat,
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

        // Tampilkan nama user dari sesi
        lblProfile.setText(UserSession.getInstance().getUsername() + " - " + UserSession.getInstance().getRole());

        // Standarisasi Navbar menggunakan NavbarHelper
        NavbarHelper.setupFullNavbar(jPanelHeader, txtSearch, lblProfile, btnLogout, "Cari ID transaksi atau kasir...");

        // Search Logic
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            private void filterTable() {
                String text = txtSearch.getText();
                javax.swing.table.TableRowSorter<?> sorter = new javax.swing.table.TableRowSorter<>(riwayatModel);
                tbl_riwayat.setRowSorter(sorter);
                if (text.equals("Cari ID transaksi atau kasir...") || text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        // Event Handlers untuk Filter
        txtTanggal.setEditable(false);
        txtTanggal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtTanggal.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                pilihTanggal();
            }
        });

        btnFilterStatus.addActionListener(e -> showStatusMenu());
    }

    private void loadRiwayat() {
        riwayatModel.setRowCount(0);
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // 1. Filter Waktu
        if (tanggalCustomAktif != null) {
            conditions.add("DATE(t.created_at) = ?");
            params.add(tanggalCustomAktif);
        } else {
            switch(filterWaktuAktif) {
                case "Hari Ini": conditions.add("DATE(t.created_at) = CURDATE()"); break;
                case "7 Hari":   conditions.add("t.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)"); break;
                case "30 Hari":  conditions.add("t.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)"); break;
            }
        }

        // 2. Filter Status
        if (!filterStatusAktif.equals("Semua")) {
            conditions.add("t.status = ?");
            params.add(filterStatusAktif);
        }
        
        String whereClause = conditions.isEmpty() ? "1=1" : String.join(" AND ", conditions);
        
        String sql = "SELECT t.id_transaksi, DATE_FORMAT(t.created_at, '%d %b %Y %H:%i') as waktu, "
                   + "u.username, t.total, t.status "
                   + "FROM transaksi t "
                   + "JOIN pemesanan p ON t.id_pemesanan = p.id_pemesanan "
                   + "JOIN user u ON p.id_user = u.id_user "
                   + "WHERE " + whereClause + " ORDER BY t.created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while(rs.next()) {
                    String noTrx = "#TRX-" + rs.getInt("id_transaksi");
                    riwayatModel.addRow(new Object[]{
                        noTrx, 
                        rs.getString("waktu"), 
                        rs.getString("username"), 
                        formatRp((int)rs.getDouble("total")), 
                        rs.getString("status")
                    });
                    count++;
                }
                lblJumlah.setText("Menampilkan " + count + " transaksi (" + filterStatusAktif + ")");
                updateFilterButtonUI();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + e.getMessage());
        }
    }

    private void updateFilterButtonUI() {
        JButton[] btns = {btnHariIni, btn7Hari, btn30Hari};
        String[] labels = {"Hari Ini", "7 Hari", "30 Hari"};
        
        for (int i = 0; i < btns.length; i++) {
            boolean active = (tanggalCustomAktif == null && filterWaktuAktif.equals(labels[i]));
            btns[i].setBackground(active ? new Color(139,29,36) : Color.WHITE);
            btns[i].setForeground(active ? Color.WHITE : new Color(80,80,80));
        }
        
        if (tanggalCustomAktif != null) {
            txtTanggal.setBorder(BorderFactory.createLineBorder(new Color(139,29,36), 1));
        } else {
            txtTanggal.setBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1));
            txtTanggal.setText("📅 Pilih Tanggal");
        }
        
        btnFilterStatus.setText("≡ Status: " + filterStatusAktif);
    }

    private void pilihTanggal() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinner, "dd MMMM yyyy");
        spinner.setEditor(dateEditor);
        spinner.setValue(new Date());

        int option = JOptionPane.showOptionDialog(this, spinner, "Pilih Tanggal",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (option == JOptionPane.OK_OPTION) {
            Date selectedDate = (Date) spinner.getValue();
            tanggalCustomAktif = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            txtTanggal.setText("📅 " + new SimpleDateFormat("dd MMM yyyy").format(selectedDate));
            loadRiwayat();
        }
    }

    private void showStatusMenu() {
        JPopupMenu menu = new JPopupMenu();
        String[] statuses = {"Semua", "Paid", "Refund"};
        for (String s : statuses) {
            JMenuItem item = new JMenuItem(s);
            item.addActionListener(e -> {
                filterStatusAktif = s;
                loadRiwayat();
            });
            menu.add(item);
        }
        menu.show(btnFilterStatus, 0, btnFilterStatus.getHeight());
    }

    private void tampilkanDetail(String idTrxString) {
        if (!jPanelDetail.isVisible()) {
            jPanelDetail.setVisible(true);
            jPanelContent.revalidate();
            jPanelContent.repaint();
        }
        
        int idTrx = Integer.parseInt(idTrxString.replace("#TRX-", ""));
        
        String sql = "SELECT t.id_transaksi, DATE_FORMAT(t.created_at, '%d %b %Y %H:%i') as waktu, "
                   + "u.username, pb.metode, t.status, t.total, t.pajak "
                   + "FROM transaksi t "
                   + "JOIN pemesanan p ON t.id_pemesanan = p.id_pemesanan "
                   + "JOIN user u ON p.id_user = u.id_user "
                   + "LEFT JOIN pembayaran pb ON pb.id_transaksi = t.id_transaksi "
                   + "WHERE t.id_transaksi = ?";
                   
        String sqlDetail = "SELECT od.jumlah, m.nama, m.harga, od.subtotal "
                         + "FROM order_detail od JOIN menu m ON od.id_menu = m.id_menu "
                         + "JOIN pemesanan p ON od.id_pemesanan = p.id_pemesanan "
                         + "JOIN transaksi t ON t.id_pemesanan = p.id_pemesanan "
                         + "WHERE t.id_transaksi = ?";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Ambil Header Detail
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idTrx);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    lblDetailNoTrx.setText("#TRX-" + rs.getInt("id_transaksi"));
                    lblDetailWaktu.setText(rs.getString("waktu"));
                    lblDetailKasir.setText(rs.getString("username"));
                    lblDetailStatus.setText(rs.getString("status"));
                    lblDetailMetode.setText(rs.getString("metode") != null ? rs.getString("metode") : "-");
                    
                    double total = rs.getDouble("total");
                    double pajak = rs.getDouble("pajak");
                    double subtotal = total - pajak;
                    
                    lblSubtotal.setText(formatRp((int)subtotal));
                    lblPajak.setText(formatRp((int)pajak));
                    lblTotal.setText(formatRp((int)total));
                }
            }
            
            // 2. Ambil List Item
            detailModel.setRowCount(0);
            try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
                ps.setInt(1, idTrx);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    detailModel.addRow(new Object[]{
                        rs.getInt("jumlah") + "x",
                        rs.getString("nama"),
                        formatRp((int)rs.getDouble("harga")),
                        formatRp((int)rs.getDouble("subtotal"))
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        jPanelDetail.setVisible(true);
    }

    private String formatRp(int n) {
        return "Rp " + NumberFormat.getInstance(new Locale("id","ID")).format(n);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelSidebar    = new JPanel();
        jLabelLogo       = new JLabel();
        jLabelSubLogo    = new JLabel();
        btnMenuDashboard = new JButton();
        btnMenuKelolaMenu= new JButton();
        btnMenuKelolaStok= new JButton();
        btnMenuKelolaPengguna = new JButton();
        btnMenuTransaksi = new JButton();
        btnMenuRiwayat   = new JButton();
        btnMenuLaporan   = new JButton();

        jPanelMain    = new JPanel();
        jPanelHeader  = new JPanel();
        txtSearch     = new JTextField();
        lblProfile    = new JLabel();
        btnLogout     = new JButton();

        jPanelContent = new JPanel();

        // Kiri — Daftar Transaksi
        jPanelKiri    = new JPanel();
        btnHariIni    = new JButton();
        btn7Hari      = new JButton();
        btn30Hari     = new JButton();
        txtTanggal    = new JTextField();
        btnFilterStatus = new JButton();
        jScrollRiwayat = new JScrollPane();
        tbl_riwayat   = new JTable();
        lblJumlah     = new JLabel();

        // Kanan — Detail Transaksi
        jPanelDetail  = new JPanel();
        lblDetailTitle = new JLabel();
        lblDetailNoTrx = new JLabel();
        lblWaktuKey   = new JLabel(); lblDetailWaktu   = new JLabel();
        lblKasirKey   = new JLabel(); lblDetailKasir   = new JLabel();
        lblMetodeKey  = new JLabel(); lblDetailMetode  = new JLabel();
        lblStatusKey  = new JLabel(); lblDetailStatus  = new JLabel();
        lblDaftarPesanan = new JLabel();
        jScrollDetail = new JScrollPane();
        tbl_detail    = new JTable();
        lblSubtotalKey= new JLabel(); lblSubtotal = new JLabel();
        lblPajakKey   = new JLabel(); lblPajak    = new JLabel();
        lblTotalKey   = new JLabel(); lblTotal    = new JLabel();
        btn_cetakStruk= new JButton();
        btn_refund    = new JButton();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Riwayat Transaksi");
        getContentPane().setLayout(new BorderLayout());

        // ── SIDEBAR ──────────────────────────────────────────
        jPanelSidebar.setBackground(new Color(139, 29, 36));
        jPanelSidebar.setPreferredSize(new Dimension(250, 700));
        jPanelSidebar.setLayout(null);

        jLabelLogo.setFont(new Font("SansSerif", Font.BOLD, 22));
        jLabelLogo.setForeground(Color.WHITE);
        jLabelLogo.setText("DoBarPOS");
        jPanelSidebar.add(jLabelLogo);
        jLabelLogo.setBounds(18, 28, 130, 30);

        jLabelSubLogo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        jLabelSubLogo.setForeground(new Color(220, 220, 220));
        jLabelSubLogo.setText("Sistem Manajemen");
        jPanelSidebar.add(jLabelSubLogo);
        jLabelSubLogo.setBounds(18, 56, 140, 16);

        String[] sideLabels = {"⊞  Dashboard","🍴  Kelola Menu","📦  Kelola Stok","👥  Kelola Pengguna","📝  Transaksi","🕐  Riwayat","📊  Laporan"};
        JButton[] sideBtns  = {btnMenuDashboard, btnMenuKelolaMenu, btnMenuKelolaStok, btnMenuKelolaPengguna, btnMenuTransaksi, btnMenuRiwayat, btnMenuLaporan};
        for (int i = 0; i < sideBtns.length; i++) {
            sideBtns[i].setBackground(i == 5 ? Color.WHITE : new Color(139, 29, 36));
            sideBtns[i].setForeground(i == 5 ? new Color(139, 29, 36) : Color.WHITE);
            sideBtns[i].setFont(new Font("SansSerif", Font.BOLD, 13));
            sideBtns[i].setText(sideLabels[i]);
            sideBtns[i].setHorizontalAlignment(SwingConstants.LEFT);
            jPanelSidebar.add(sideBtns[i]);
            sideBtns[i].setBounds(10, 95 + i * 46, 230, 40);
        }
        getContentPane().add(jPanelSidebar, BorderLayout.WEST);

        // ── HEADER ───────────────────────────────────────────
        jPanelMain.setBackground(new Color(252, 246, 246));
        jPanelMain.setLayout(new BorderLayout());

        jPanelHeader.setBackground(Color.WHITE);
        jPanelHeader.setPreferredSize(new Dimension(900, 60));
        jPanelHeader.setLayout(new BorderLayout());

        JPanel hL = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        hL.setOpaque(false);
        txtSearch.setText("Cari transaksi...");
        txtSearch.setPreferredSize(new Dimension(260, 38));
        hL.add(txtSearch);
        jPanelHeader.add(hL, BorderLayout.WEST);

        JPanel hR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        hR.setOpaque(false);
        lblProfile.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblProfile.setText("Yaya - Kasir");
        btnLogout.setText("Logout");
        btnLogout.setPreferredSize(new Dimension(80, 36));
        hR.add(lblProfile); hR.add(btnLogout);
        jPanelHeader.add(hR, BorderLayout.EAST);
        jPanelMain.add(jPanelHeader, BorderLayout.NORTH);

        // ── CONTENT (Kiri + Kanan) ────────────────────────────
        jPanelContent.setBackground(new Color(252, 246, 246));
        jPanelContent.setLayout(new BorderLayout(12, 0));
        jPanelContent.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));

        // ── PANEL KIRI — DAFTAR TRANSAKSI ────────────────────
        jPanelKiri.setBackground(Color.WHITE);
        jPanelKiri.setLayout(new BorderLayout());

        // Panel toolbar filter (NORTH dari jPanelKiri)
        JPanel jPanelToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        jPanelToolbar.setBackground(Color.WHITE);
        jPanelToolbar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(230,230,230)));

        JButton[] filterBtns = {btnHariIni, btn7Hari, btn30Hari};
        String[]  filterLbl  = {"Hari Ini","7 Hari","30 Hari"};
        for (int i = 0; i < filterBtns.length; i++) {
            filterBtns[i].setText(filterLbl[i]);
            filterBtns[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
            filterBtns[i].setBackground(i == 0 ? new Color(139,29,36) : Color.WHITE);
            filterBtns[i].setForeground(i == 0 ? Color.WHITE : new Color(80,80,80));
            filterBtns[i].setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
            filterBtns[i].setPreferredSize(new Dimension(78, 30));
            jPanelToolbar.add(filterBtns[i]);
            final String lbl = filterLbl[i];
            filterBtns[i].addActionListener(e -> filterByWaktu(lbl));
        }

        txtTanggal.setText("📅 Pilih Tanggal");
        txtTanggal.setFont(new Font("SansSerif", Font.PLAIN, 11));
        txtTanggal.setPreferredSize(new Dimension(130, 30));
        jPanelToolbar.add(txtTanggal);

        btnFilterStatus.setText("≡ Filter Status");
        btnFilterStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnFilterStatus.setPreferredSize(new Dimension(110, 30));
        btnFilterStatus.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        jPanelToolbar.add(btnFilterStatus);

        jPanelKiri.add(jPanelToolbar, BorderLayout.NORTH);

        // JTable tbl_riwayat — Row Selection, tidak bisa diedit
        riwayatModel = new DefaultTableModel(
            new String[]{"NO. TRANSAKSI","WAKTU","KASIR","TOTAL","STATUS"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; } // Tidak bisa diedit
        };
        tbl_riwayat.setModel(riwayatModel);
        tbl_riwayat.setRowHeight(42);
        tbl_riwayat.setShowGrid(false);
        tbl_riwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Row Selection
        tbl_riwayat.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tbl_riwayat.getTableHeader().setBackground(new Color(252, 246, 246));
        tbl_riwayat.getColumnModel().getColumn(0).setPreferredWidth(140);
        tbl_riwayat.getColumnModel().getColumn(3).setPreferredWidth(80);
        tbl_riwayat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                tbl_riwayatMouseClicked(e);
            }
        });
        jScrollRiwayat.setViewportView(tbl_riwayat);
        jScrollRiwayat.setBorder(null);
        // Gunakan BorderLayout: scrollpane di CENTER agar mengisi penuh
        jPanelKiri.add(jScrollRiwayat, BorderLayout.CENTER);

        // Footer label jumlah (SOUTH dari jPanelKiri)
        JPanel jPanelFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        jPanelFooter.setBackground(Color.WHITE);
        jPanelFooter.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(230,230,230)));
        lblJumlah.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblJumlah.setForeground(new Color(120,120,120));
        lblJumlah.setText("Menampilkan data...");
        jPanelFooter.add(lblJumlah);
        jPanelKiri.add(jPanelFooter, BorderLayout.SOUTH);

        jPanelContent.add(jPanelKiri, BorderLayout.CENTER);

        // ── PANEL KANAN — DETAIL TRANSAKSI ───────────────────
        jPanelDetail.setBackground(Color.WHITE);
        jPanelDetail.setPreferredSize(new Dimension(330, 600));
        jPanelDetail.setBorder(BorderFactory.createMatteBorder(0,1,0,0,new Color(220,220,220)));
        jPanelDetail.setLayout(null);
        jPanelDetail.setVisible(false); // Tersembunyi sampai baris diklik

        lblDetailTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblDetailTitle.setText("Detail Transaksi");
        jPanelDetail.add(lblDetailTitle);
        lblDetailTitle.setBounds(16, 14, 220, 26);

        lblDetailNoTrx.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDetailNoTrx.setForeground(new Color(139,29,36));
        jPanelDetail.add(lblDetailNoTrx);
        lblDetailNoTrx.setBounds(16, 42, 280, 18);

        // Grid info (Waktu, Kasir, Metode, Status)
        String[] keys = {"Waktu","Kasir","Metode Bayar","Status"};
        JLabel[] keyLbls = {lblWaktuKey, lblKasirKey, lblMetodeKey, lblStatusKey};
        JLabel[] valLbls = {lblDetailWaktu, lblDetailKasir, lblDetailMetode, lblDetailStatus};
        int[] kx = {16,170,16,170}, ky = {70,70,102,102};
        for (int i = 0; i < keys.length; i++) {
            keyLbls[i].setFont(new Font("SansSerif", Font.PLAIN, 11));
            keyLbls[i].setForeground(new Color(120,120,120));
            keyLbls[i].setText(keys[i]);
            jPanelDetail.add(keyLbls[i]);
            keyLbls[i].setBounds(kx[i], ky[i], 120, 16);

            valLbls[i].setFont(new Font("SansSerif", Font.BOLD, 12));
            jPanelDetail.add(valLbls[i]);
            valLbls[i].setBounds(kx[i], ky[i]+18, 140, 18);
        }

        lblDaftarPesanan.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDaftarPesanan.setText("DAFTAR PESANAN");
        jPanelDetail.add(lblDaftarPesanan);
        lblDaftarPesanan.setBounds(16, 148, 170, 18);

        // JTable tbl_detail (Daftar pesanan per transaksi)
        detailModel = new DefaultTableModel(new String[]{"Qty","Menu","Harga","Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_detail.setModel(detailModel);
        tbl_detail.setRowHeight(38);
        tbl_detail.setShowGrid(false);
        tbl_detail.getColumnModel().getColumn(0).setPreferredWidth(35);
        jScrollDetail.setViewportView(tbl_detail);
        jScrollDetail.setBorder(null);
        jPanelDetail.add(jScrollDetail);
        jScrollDetail.setBounds(8, 170, 304, 200);

        // Ringkasan Harga
        JLabel[] sumKeys = {lblSubtotalKey, lblPajakKey, lblTotalKey};
        JLabel[] sumVals = {lblSubtotal, lblPajak, lblTotal};
        String[] sumLabels = {"Subtotal","Pajak (10%)","Total"};
        for (int i = 0; i < sumKeys.length; i++) {
            sumKeys[i].setFont(new Font("SansSerif", i==2?Font.BOLD:Font.PLAIN, i==2?14:12));
            sumKeys[i].setText(sumLabels[i]);
            jPanelDetail.add(sumKeys[i]);
            sumKeys[i].setBounds(16, 380 + i*28, 120, 20);

            sumVals[i].setFont(new Font("SansSerif", i==2?Font.BOLD:Font.PLAIN, i==2?14:12));
            sumVals[i].setForeground(i==2 ? new Color(139,29,36) : Color.BLACK);
            sumVals[i].setHorizontalAlignment(SwingConstants.RIGHT);
            jPanelDetail.add(sumVals[i]);
            sumVals[i].setBounds(160, 380 + i*28, 152, 20);
        }

        // Tombol Aksi
        btn_cetakStruk.setText("🖨 Cetak Struk");
        btn_cetakStruk.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn_cetakStruk.setBackground(Color.WHITE);
        btn_cetakStruk.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        btn_cetakStruk.addActionListener(e -> btn_cetakStrukActionPerformed());
        jPanelDetail.add(btn_cetakStruk);
        btn_cetakStruk.setBounds(10, 470, 140, 40);

        btn_refund.setText("↩ Refund");
        btn_refund.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn_refund.setBackground(new Color(252, 225, 225));
        btn_refund.setForeground(new Color(139,29,36));
        btn_refund.setBorder(BorderFactory.createLineBorder(new Color(230,200,200)));
        btn_refund.addActionListener(e -> btn_refundActionPerformed());
        jPanelDetail.add(btn_refund);
        btn_refund.setBounds(160, 470, 152, 40);

        jPanelContent.add(jPanelDetail, BorderLayout.EAST);
        jPanelMain.add(jPanelContent, BorderLayout.CENTER);
        getContentPane().add(jPanelMain, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // ── Event Handlers ────────────────────────────────────────

    private void filterByWaktu(String label) {
        System.out.println("Filter aktif: " + label);
        this.filterWaktuAktif = label;
        this.tanggalCustomAktif = null;
        loadRiwayat();
    }

    private void tbl_riwayatMouseClicked(java.awt.event.MouseEvent e) {
        int row = tbl_riwayat.getSelectedRow();
        if (row < 0) return;
        String idTrxString = riwayatModel.getValueAt(row, 0).toString();
        tampilkanDetail(idTrxString);
    }

    private void btn_cetakStrukActionPerformed() {
        if (detailModel.getRowCount() == 0) return;
        
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("               DoBarPOS                 \n");
        sb.append("        Sistem Manajemen Kasir          \n");
        sb.append("========================================\n");
        sb.append("No Trx : ").append(lblDetailNoTrx.getText()).append("\n");
        sb.append("Kasir  : ").append(lblDetailKasir.getText()).append("\n");
        sb.append("Waktu  : ").append(lblDetailWaktu.getText()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-24s %15s\n", "ITEM", "SUBTOTAL"));
        sb.append("----------------------------------------\n");
        for (int i = 0; i < detailModel.getRowCount(); i++) {
            String qty = detailModel.getValueAt(i, 0).toString();
            String nama = detailModel.getValueAt(i, 1).toString();
            String subtotal = detailModel.getValueAt(i, 3).toString();
            
            String itemStr = nama + " " + qty;
            if (itemStr.length() > 24) {
                itemStr = itemStr.substring(0, 24);
            }
            sb.append(String.format("%-24s %15s\n", itemStr, subtotal));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-24s %15s\n", "Subtotal", lblSubtotal.getText()));
        sb.append(String.format("%-24s %15s\n", "Pajak (10%)", lblPajak.getText()));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-24s %15s\n", "TOTAL", lblTotal.getText()));
        sb.append("========================================\n");
        sb.append("           Terima Kasih Atas            \n");
        sb.append("            Kunjungan Anda!             \n");
        sb.append("========================================\n");
        
        String strukText = sb.toString();
        
        JTextArea textArea = new JTextArea(strukText);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setOpaque(false);
        
        Object[] options = {"Cetak PDF", "Tutup"};
        int n = JOptionPane.showOptionDialog(this,
            textArea,
            "Cetak Struk",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[1]);
            
        if (n == 0) { // Cetak PDF
            cetakPDF(strukText, lblDetailNoTrx.getText());
        }
    }

    private void cetakPDF(String teksStruk, String noTrx) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Struk PDF");
        fileChooser.setSelectedFile(new java.io.File("Struk_" + noTrx.replace("#", "") + ".pdf"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new java.io.File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
            }
            try {
                // Kalkulasi ukuran halaman seperti roll paper thermal printer
                String[] lines = teksStruk.split("\n");
                int lineCount = lines.length;
                float fontSize = 11f;
                float leading = 15f; 
                float marginY = 20f;
                float marginX = 20f;
                
                // Lebar 40 karakter Courier @11pt = ~264pt. Kita set ke 340pt agar muat + margin
                float width = 340f; 
                float height = (lineCount * leading) + (marginY * 2) + 20f;
                
                com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(width, height);
                com.itextpdf.text.Document document = new com.itextpdf.text.Document(pageSize, marginX, marginX, marginY, marginY);
                
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fileToSave));
                document.open();
                
                com.itextpdf.text.Font font = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, fontSize, com.itextpdf.text.Font.NORMAL);
                // Gunakan konstruktor Paragraph dengan leading spesifik agar jarak baris pas dengan tinggi kertas
                com.itextpdf.text.Paragraph paragraph = new com.itextpdf.text.Paragraph(leading, teksStruk, font);
                paragraph.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                document.add(paragraph);
                
                document.close();
                JOptionPane.showMessageDialog(this, "Struk PDF berhasil disimpan di:\n" + fileToSave.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(fileToSave);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btn_refundActionPerformed() {
        if (lblDetailStatus.getText().equals("Refund")) {
            JOptionPane.showMessageDialog(this, "Transaksi ini sudah berstatus Refund.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Proses Refund untuk " + lblDetailNoTrx.getText() + "?",
            "Konfirmasi Refund", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int idTrx = Integer.parseInt(lblDetailNoTrx.getText().replace("#TRX-", ""));
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE transaksi SET status='Refund' WHERE id_transaksi=?")) {
                ps.setInt(1, idTrx);
                ps.executeUpdate();
                lblDetailStatus.setText("Refund");
                int row = tbl_riwayat.getSelectedRow();
                if (row >= 0) riwayatModel.setValueAt("Refund", row, 4);
                JOptionPane.showMessageDialog(this, "Berhasil refund " + lblDetailNoTrx.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal refund: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new RiwayatFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnLogout, btnMenuDashboard, btnMenuKelolaMenu;
    private JButton btnMenuKelolaStok, btnMenuKelolaPengguna, btnMenuTransaksi, btnMenuRiwayat, btnMenuLaporan;
    private JButton btnHariIni, btn7Hari, btn30Hari, btnFilterStatus;
    private JButton btn_cetakStruk, btn_refund;
    private JLabel jLabelLogo, jLabelSubLogo, lblProfile, lblJumlah;
    private JLabel lblDetailTitle, lblDetailNoTrx;
    private JLabel lblWaktuKey, lblDetailWaktu, lblKasirKey, lblDetailKasir;
    private JLabel lblMetodeKey, lblDetailMetode, lblStatusKey, lblDetailStatus;
    private JLabel lblDaftarPesanan;
    private JLabel lblSubtotalKey, lblSubtotal, lblPajakKey, lblPajak, lblTotalKey, lblTotal;
    private JPanel jPanelSidebar, jPanelMain, jPanelHeader, jPanelContent;
    private JPanel jPanelKiri, jPanelDetail;
    private JScrollPane jScrollRiwayat, jScrollDetail;
    private JTable tbl_riwayat, tbl_detail;
    private JTextField txtSearch, txtTanggal;
    // End of variables declaration//GEN-END:variables
}
