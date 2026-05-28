package dobarpos;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TransaksiFrame extends JFrame {

    private DefaultTableModel cartModel;
    private ButtonGroup bgMetode;
    private double PAJAK_RATE = 0.10;

    public TransaksiFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadMenuKatalog("Semua Menu");
        hitungTotal();

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
        NavigationHelper.setActiveButton(btnMenuTransaksi,
            btnMenuDashboard, btnMenuKelolaMenu, btnMenuKelolaStok,
            btnMenuKelolaPengguna, btnMenuTransaksi, btnMenuRiwayat, btnMenuLaporan);

        // Navigasi listener
        btnMenuDashboard.addActionListener(e -> NavigationHelper.navigateTo(this, "Dashboard"));
        btnMenuKelolaMenu.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaMenu"));
        btnMenuKelolaStok.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaStok"));
        btnMenuKelolaPengguna.addActionListener(e -> NavigationHelper.navigateTo(this, "KelolaPengguna"));
        btnMenuTransaksi.addActionListener(e -> NavigationHelper.navigateTo(this, "Transaksi"));
        btnMenuRiwayat.addActionListener(e -> NavigationHelper.navigateTo(this, "Riwayat"));
        btnMenuLaporan.addActionListener(e -> NavigationHelper.navigateTo(this, "Laporan"));
        btnLogout.addActionListener(e -> NavigationHelper.logout(this));

        // Tampilkan session user
        lblProfile.setText(UserSession.getInstance().getUsername() + " - " + UserSession.getInstance().getRole());

        // Standarisasi Navbar menggunakan NavbarHelper
        NavbarHelper.setupFullNavbar(jPanelHeader, txtSearchHeader, lblProfile, btnLogout, "Cari produk untuk dipesan...");

        // Search Logic
        txtSearchHeader.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            private void doSearch() {
                String kw = txtSearchHeader.getText();
                if (kw.equals("Cari produk untuk dipesan...") || kw.trim().isEmpty()) kw = "";
                // Reset filter kategori saat mencari
                loadMenuKatalogFiltered("Semua Menu", kw);
            }
        });
    }

    // =========================================================
    // Membangun kartu menu secara dinamis di dalam jPanelGrid
    // =========================================================
    private void loadMenuKatalog(String filter) {
        loadMenuKatalogFiltered(filter, "");
    }

    private void loadMenuKatalogFiltered(String filter, String keyword) {
        jPanelGrid.removeAll();
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT id_menu, nama, harga FROM menu WHERE nama LIKE ?";
            if (!filter.equals("Semua Menu")) {
                sql += " AND kategori = ?";
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            if (!filter.equals("Semua Menu")) {
                ps.setString(2, filter);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jPanelGrid.add(buatKartuMenu(rs.getInt("id_menu"), rs.getString("nama"), rs.getInt("harga")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat menu: " + e.getMessage());
        }
        jPanelGrid.revalidate();
        jPanelGrid.repaint();
    }

    private JPanel buatKartuMenu(int id, String nama, int harga) {
        JPanel kartu = new JPanel(null);
        kartu.setPreferredSize(new Dimension(140, 180));
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        kartu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Gambar placeholder
        JPanel imgPanel = new JPanel(new GridBagLayout()); // Gunakan GridBagLayout agar icon ke tengah
        imgPanel.setBackground(new Color(245, 235, 235));
        JLabel imgLbl = new JLabel("☕", SwingConstants.CENTER);
        imgLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        imgPanel.add(imgLbl);
        imgPanel.setBounds(0, 0, 140, 110);
        kartu.add(imgPanel);

        JLabel lblNama = new JLabel("<html><center>" + nama + "</center></html>", SwingConstants.CENTER);
        lblNama.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblNama.setBounds(5, 112, 130, 30);
        kartu.add(lblNama);

        JLabel lblHarga = new JLabel(formatRupiah(harga), SwingConstants.CENTER);
        lblHarga.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblHarga.setForeground(new Color(139, 29, 36));
        lblHarga.setBounds(5, 142, 130, 20);
        kartu.add(lblHarga);

        kartu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                tambahKeKeranjang(id, nama, harga);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                kartu.setBackground(new Color(252, 245, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                kartu.setBackground(Color.WHITE);
            }
        });
        return kartu;
    }

    // =========================================================
    // Logika Keranjang
    // =========================================================
    private void tambahKeKeranjang(int id, String nama, int harga) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (Integer.parseInt(cartModel.getValueAt(i, 0).toString()) == id) {
                int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString()) + 1;
                cartModel.setValueAt(qty, i, 2);
                cartModel.setValueAt(harga * qty, i, 3);
                hitungTotal();
                return;
            }
        }
        cartModel.addRow(new Object[]{id, nama, 1, harga});
        hitungTotal();
    }

    private void hitungTotal() {
        double subtotal = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            subtotal += Double.parseDouble(cartModel.getValueAt(i, 3).toString());
        }
        double pajak  = subtotal * PAJAK_RATE;
        double total  = subtotal + pajak;
        lbl_pajak.setText(formatRupiah((int) pajak));
        lbl_totalHarga.setText(formatRupiah((int) total));
    }

    private String formatRupiah(int angka) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("id", "ID"));
        return "Rp " + fmt.format(angka);
    }

    private int getHargaMenu(int idMenu) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT harga FROM menu WHERE id_menu = ?");
            ps.setInt(1, idMenu);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("harga");
        } catch (SQLException e) { /* fallback */ }
        return 0;
    }

    // =========================================================
    // initComponents — Generated Code
    // =========================================================
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgMetode = new ButtonGroup();

        jPanelSidebar       = new JPanel();
        jLabelLogo          = new JLabel();
        jLabelSubLogo       = new JLabel();
        btnMenuDashboard    = new JButton();
        btnMenuKelolaMenu   = new JButton();
        btnMenuKelolaStok   = new JButton();
        btnMenuKelolaPengguna = new JButton();
        btnMenuTransaksi    = new JButton();
        btnMenuRiwayat      = new JButton();
        btnMenuLaporan      = new JButton();

        jPanelMain          = new JPanel();
        jPanelHeader        = new JPanel();
        txtSearchHeader     = new JTextField();
        lblProfile          = new JLabel();
        btnLogout           = new JButton();

        // Area Tengah (Katalog)
        jPanelKatalogWrapper = new JPanel();
        jPanelFilter        = new JPanel();
        btnFilterSemua      = new JToggleButton();
        btnFilterKopi       = new JToggleButton();
        btnFilterNonKopi    = new JToggleButton();
        btnFilterMakanan    = new JToggleButton();
        btnFilterMakananUtama = new JToggleButton();
        jScrollKatalog      = new JScrollPane();
        jPanelGrid          = new JPanel();

        // Area Kanan (Keranjang)
        jPanelKeranjang     = new JPanel();
        lblKeranjangTitle   = new JLabel();
        jScrollKeranjang    = new JScrollPane();
        tbl_keranjang       = new JTable();
        lblPajakLabel       = new JLabel();
        lbl_pajak           = new JLabel();
        lblTotalLabel       = new JLabel();
        lbl_totalHarga      = new JLabel();
        lblMetodeLabel      = new JLabel();
        rbCash              = new JToggleButton();
        rbQRIS              = new JToggleButton();
        rbDebit             = new JToggleButton();
        btn_cetakStruk      = new JButton();
        btn_prosesBayar     = new JButton();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Transaksi");
        getContentPane().setLayout(new BorderLayout());

        // --- SIDEBAR ---
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
        jLabelSubLogo.setBounds(18, 56, 130, 16);

        String[] sideLabels  = {"⊞  Dashboard","🍴  Kelola Menu","📦  Kelola Stok","👥  Kelola Pengguna","📝  Transaksi","🕐  Riwayat","📊  Laporan"};
        JButton[] sideBtns   = {btnMenuDashboard, btnMenuKelolaMenu, btnMenuKelolaStok, btnMenuKelolaPengguna, btnMenuTransaksi, btnMenuRiwayat, btnMenuLaporan};
        for (int i = 0; i < sideLabels.length; i++) {
            sideBtns[i].setBackground(i == 4 ? Color.WHITE : new Color(139, 29, 36));
            sideBtns[i].setForeground(i == 4 ? new Color(139, 29, 36) : Color.WHITE);
            sideBtns[i].setFont(new Font("SansSerif", Font.BOLD, 13));
            sideBtns[i].setText(sideLabels[i]);
            sideBtns[i].setHorizontalAlignment(SwingConstants.LEFT);
            jPanelSidebar.add(sideBtns[i]);
            sideBtns[i].setBounds(10, 95 + i * 46, 230, 40);
        }
        getContentPane().add(jPanelSidebar, BorderLayout.WEST);

        // --- MAIN PANEL ---
        jPanelMain.setBackground(new Color(252, 246, 246));
        jPanelMain.setLayout(new BorderLayout());

        // Header
        jPanelHeader.setBackground(Color.WHITE);
        jPanelHeader.setPreferredSize(new Dimension(900, 60));
        jPanelHeader.setLayout(new BorderLayout());

        JPanel hdrLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        hdrLeft.setOpaque(false);
        txtSearchHeader.setText("Cari menu...");
        txtSearchHeader.setPreferredSize(new Dimension(260, 38));
        hdrLeft.add(txtSearchHeader);
        jPanelHeader.add(hdrLeft, BorderLayout.WEST);

        JPanel hdrRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        hdrRight.setOpaque(false);
        lblProfile.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblProfile.setText("Yaya - Kasir");
        btnLogout.setText("Logout");
        btnLogout.setPreferredSize(new Dimension(80, 36));
        hdrRight.add(lblProfile);
        hdrRight.add(btnLogout);
        jPanelHeader.add(hdrRight, BorderLayout.EAST);

        jPanelMain.add(jPanelHeader, BorderLayout.NORTH);

        // Panel untuk menampung Katalog dan Keranjang dengan gap
        JPanel jPanelContent = new JPanel(new BorderLayout());
        jPanelContent.setOpaque(false);
        jPanelContent.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));

        // --- CENTER: Katalog Menu ---
        jPanelKatalogWrapper.setBackground(new Color(252, 246, 246));
        jPanelKatalogWrapper.setLayout(new BorderLayout(0, 0));

        // Filter tombol kategori — sesuai ENUM di DB
        jPanelFilter.setBackground(new Color(252, 246, 246));
        jPanelFilter.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        JToggleButton[] filterBtns = {btnFilterSemua, btnFilterKopi, btnFilterNonKopi, btnFilterMakanan, btnFilterMakananUtama};
        String[] filterLabels = {"Semua Menu", "Kopi", "Non-Kopi", "Makanan", "Minuman"};
        String[] filterValues = {"Semua Menu", "Kopi", "Non-Kopi", "Makanan", "Minuman"};
        ButtonGroup bgFilter = new ButtonGroup();
        Color activeColor = new Color(139, 29, 36);
        Color inactiveColor = new Color(230, 230, 230);
        for (int i = 0; i < filterBtns.length; i++) {
            filterBtns[i].setText(filterLabels[i]);
            filterBtns[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
            filterBtns[i].setFocusPainted(false);
            filterBtns[i].setBackground(inactiveColor);
            filterBtns[i].setForeground(Color.DARK_GRAY);
            final String val = filterValues[i];
            final JToggleButton btn = filterBtns[i];
            filterBtns[i].addItemListener(ev -> {
                if (ev.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                    btn.setBackground(activeColor);
                    btn.setForeground(Color.WHITE);
                    loadMenuKatalog(val);
                } else {
                    btn.setBackground(inactiveColor);
                    btn.setForeground(Color.DARK_GRAY);
                }
            });
            bgFilter.add(filterBtns[i]);
            jPanelFilter.add(filterBtns[i]);
        }
        btnFilterSemua.setSelected(true);
        btnFilterSemua.setBackground(activeColor);
        btnFilterSemua.setForeground(Color.WHITE);
        jPanelKatalogWrapper.add(jPanelFilter, BorderLayout.NORTH);

        // Grid kartu menu (WrapLayout → pakai GridLayout dinamis)
        jPanelGrid.setBackground(new Color(252, 246, 246));
        jPanelGrid.setLayout(new WrapLayout(FlowLayout.LEFT, 14, 14));
        jScrollKatalog.setViewportView(jPanelGrid);
        jScrollKatalog.setBorder(null);
        jScrollKatalog.getViewport().setBackground(new Color(252, 246, 246));
        jPanelKatalogWrapper.add(jScrollKatalog, BorderLayout.CENTER);

        jPanelContent.add(jPanelKatalogWrapper, BorderLayout.CENTER);

        // --- EAST: Keranjang ---
        jPanelKeranjang.setBackground(Color.WHITE);
        jPanelKeranjang.setPreferredSize(new Dimension(290, 600));
        jPanelKeranjang.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(225, 225, 225)));
        jPanelKeranjang.setLayout(null);

        lblKeranjangTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblKeranjangTitle.setText("🧾 Pesanan Saat Ini");
        jPanelKeranjang.add(lblKeranjangTitle);
        lblKeranjangTitle.setBounds(14, 14, 240, 24);

        // Tabel Keranjang — kolom Qty bisa diedit
        cartModel = new DefaultTableModel(new String[]{"ID","Item","Qty","Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 2; } // hanya kolom Qty
        };
        tbl_keranjang.setModel(cartModel);
        // Listener: saat Qty diedit, update Subtotal dan Total
        cartModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 2) {
                int row = e.getFirstRow();
                try {
                    int newQty = Integer.parseInt(cartModel.getValueAt(row, 2).toString());
                    if (newQty <= 0) {
                        cartModel.removeRow(row);
                    } else {
                        // Hitung harga per item dari subtotal lama / qty lama
                        // Simpan harga asli dari kolom Subtotal lama dibagi qty sebelumnya
                        // Cara mudah: ambil subtotal / qty saat ini (pakai default row)
                        // Kita perlu harga per item — cari dari nama menu di DB
                        // Simpan di kolom hidden: cartModel kolom 3 = subtotal, kolom 0 = id_menu
                        int idMenu = Integer.parseInt(cartModel.getValueAt(row, 0).toString());
                        int harga = getHargaMenu(idMenu);
                        cartModel.setValueAt(harga * newQty, row, 3);
                    }
                } catch (NumberFormatException ex) { /* abaikan input non-angka */ }
                hitungTotal();
            }
        });
        tbl_keranjang.setRowHeight(36);
        tbl_keranjang.setShowGrid(false);
        // Sembunyikan kolom ID (index 0) agar tidak terlihat user, namun tetap ada didata
        tbl_keranjang.getColumnModel().getColumn(0).setMinWidth(0);
        tbl_keranjang.getColumnModel().getColumn(0).setMaxWidth(0);
        tbl_keranjang.getColumnModel().getColumn(0).setWidth(0);
        tbl_keranjang.getColumnModel().getColumn(2).setPreferredWidth(35);
        jScrollKeranjang.setViewportView(tbl_keranjang);
        jPanelKeranjang.add(jScrollKeranjang);
        jScrollKeranjang.setBounds(10, 46, 268, 310);

        // Pajak
        lblPajakLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPajakLabel.setForeground(new Color(100, 100, 100));
        lblPajakLabel.setText("Pajak (PB1 10%)");
        jPanelKeranjang.add(lblPajakLabel);
        lblPajakLabel.setBounds(14, 368, 140, 18);

        lbl_pajak.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl_pajak.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl_pajak.setText("Rp 0");
        jPanelKeranjang.add(lbl_pajak);
        lbl_pajak.setBounds(155, 368, 120, 18);

        // Total box
        JPanel totalBox = new JPanel(null);
        totalBox.setBackground(new Color(139, 29, 36));
        totalBox.setBounds(10, 394, 268, 68);
        lblTotalLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblTotalLabel.setForeground(new Color(230, 200, 200));
        lblTotalLabel.setText("TOTAL HARGA");
        lblTotalLabel.setBounds(10, 6, 248, 14);
        lblTotalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalBox.add(lblTotalLabel);
        lbl_totalHarga.setFont(new Font("SansSerif", Font.BOLD, 22));
        lbl_totalHarga.setForeground(Color.WHITE);
        lbl_totalHarga.setText("Rp 0");
        lbl_totalHarga.setBounds(0, 22, 268, 36);
        lbl_totalHarga.setHorizontalAlignment(SwingConstants.CENTER);
        totalBox.add(lbl_totalHarga);
        jPanelKeranjang.add(totalBox);

        // Metode Pembayaran
        lblMetodeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblMetodeLabel.setForeground(new Color(100, 100, 100));
        lblMetodeLabel.setText("METODE PEMBAYARAN");
        jPanelKeranjang.add(lblMetodeLabel);
        lblMetodeLabel.setBounds(14, 472, 200, 16);

        rbCash.setText("💵 Cash");    rbCash.setSelected(true);
        rbQRIS.setText("📱 QRIS");
        rbDebit.setText("💳 Debit");
        JToggleButton[] metodes = {rbCash, rbQRIS, rbDebit};
        int[] mX = {10, 102, 193};
        for (int i = 0; i < metodes.length; i++) {
            metodes[i].setFont(new Font("SansSerif", Font.PLAIN, 11));
            metodes[i].setFocusPainted(false);
            bgMetode.add(metodes[i]);
            jPanelKeranjang.add(metodes[i]);
            metodes[i].setBounds(mX[i], 494, 85, 32);
        }

        // Tombol Aksi
        btn_cetakStruk.setBackground(Color.WHITE);
        btn_cetakStruk.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn_cetakStruk.setText("🖨 Cetak Struk");
        btn_cetakStruk.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btn_cetakStruk.addActionListener(e -> btn_cetakStrukActionPerformed());
        jPanelKeranjang.add(btn_cetakStruk);
        btn_cetakStruk.setBounds(10, 538, 124, 44);

        btn_prosesBayar.setBackground(new Color(139, 29, 36));
        btn_prosesBayar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn_prosesBayar.setForeground(Color.WHITE);
        btn_prosesBayar.setText("✔ Proses");
        btn_prosesBayar.addActionListener(e -> btn_prosesBayarActionPerformed());
        jPanelKeranjang.add(btn_prosesBayar);
        btn_prosesBayar.setBounds(143, 538, 134, 44);

        jPanelContent.add(jPanelKeranjang, BorderLayout.EAST);
        jPanelMain.add(jPanelContent, BorderLayout.CENTER);
        
        getContentPane().add(jPanelMain, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // =========================================================
    // Event Handlers
    // =========================================================

    private void btn_cetakStrukActionPerformed() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang kosong!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm");
        String currentDate = sdf.format(new java.util.Date());
        String kasirName = UserSession.getInstance().getUsername();
        
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("               DoBarPOS                 \n");
        sb.append("        Sistem Manajemen Kasir          \n");
        sb.append("========================================\n");
        sb.append("Kasir  : ").append(kasirName).append("\n");
        sb.append("Waktu  : ").append(currentDate).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-24s %15s\n", "ITEM", "SUBTOTAL"));
        sb.append("----------------------------------------\n");
        
        double subtotalNum = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String nama = cartModel.getValueAt(i, 1).toString();
            String qty = "x" + cartModel.getValueAt(i, 2).toString();
            double sub = Double.parseDouble(cartModel.getValueAt(i, 3).toString());
            subtotalNum += sub;
            String subtotal = formatRupiah((int) sub);
            
            String itemStr = nama + " " + qty;
            if (itemStr.length() > 24) {
                itemStr = itemStr.substring(0, 24);
            }
            sb.append(String.format("%-24s %15s\n", itemStr, subtotal));
        }
        
        String subtotalStr = formatRupiah((int) subtotalNum);

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-24s %15s\n", "Subtotal", subtotalStr));
        sb.append(String.format("%-24s %15s\n", "Pajak (10%)", lbl_pajak.getText()));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-24s %15s\n", "TOTAL", lbl_totalHarga.getText()));
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
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            cetakPDF(strukText, "Pesanan_" + timestamp);
        }
    }

    private void cetakPDF(String teksStruk, String defaultName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Struk PDF");
        fileChooser.setSelectedFile(new java.io.File(defaultName + ".pdf"));
        
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

    private void btn_prosesBayarActionPerformed() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang pesanan masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String metode = rbCash.isSelected() ? "Cash" : rbQRIS.isSelected() ? "QRIS" : "Debit";
        double subtotal = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            subtotal += Double.parseDouble(cartModel.getValueAt(i, 3).toString());
        }
        double pajak = subtotal * PAJAK_RATE;
        double total = subtotal + pajak;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi DB

            // STEP 1: INSERT ke tabel pemesanan
            String sqlPesan = "INSERT INTO pemesanan (id_user, status) VALUES (?, 'Selesai')";
            PreparedStatement ps1 = conn.prepareStatement(sqlPesan, Statement.RETURN_GENERATED_KEYS);
            ps1.setInt(1, UserSession.getInstance().getIdUser()); // id_user kasir yg login
            ps1.executeUpdate();
            ResultSet rs = ps1.getGeneratedKeys();
            rs.next();
            int idPemesanan = rs.getInt(1);

            // STEP 2: Looping keranjang → INSERT ke order_detail
            String sqlDetail = "INSERT INTO order_detail (id_pemesanan, id_menu, jumlah, subtotal) VALUES (?, ?, ?, ?)";
            PreparedStatement ps2 = conn.prepareStatement(sqlDetail);
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int idMenu = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                int qty    = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                double sub = Double.parseDouble(cartModel.getValueAt(i, 3).toString());
                
                ps2.setInt(1, idPemesanan); 
                ps2.setInt(2, idMenu);
                ps2.setInt(3, qty); 
                ps2.setDouble(4, sub);
                ps2.addBatch();
            }
            ps2.executeBatch();

            // STEP 3: INSERT ke tabel transaksi
            String sqlTrx = "INSERT INTO transaksi (id_pemesanan, total, pajak, status) VALUES (?, ?, ?, 'Paid')";
            PreparedStatement ps3 = conn.prepareStatement(sqlTrx, Statement.RETURN_GENERATED_KEYS);
            ps3.setInt(1, idPemesanan); 
            ps3.setDouble(2, total); 
            ps3.setDouble(3, pajak);
            ps3.executeUpdate();
            ResultSet rs3 = ps3.getGeneratedKeys(); 
            rs3.next();
            int idTransaksi = rs3.getInt(1);

            // STEP 4: INSERT ke tabel pembayaran
            String sqlBayar = "INSERT INTO pembayaran (id_transaksi, metode, jumlah_bayar) VALUES (?, ?, ?)";
            PreparedStatement ps4 = conn.prepareStatement(sqlBayar);
            ps4.setInt(1, idTransaksi); 
            ps4.setString(2, metode); 
            ps4.setDouble(3, total);
            ps4.executeUpdate();

            conn.commit(); // Semua berhasil → Commit ke database
            
            JOptionPane.showMessageDialog(this,
                "Pembayaran " + metode + " sebesar " + formatRupiah((int) total) + " BERHASIL!\nStruk siap dicetak.",
                "Transaksi Berhasil", JOptionPane.INFORMATION_MESSAGE);

            // Kosongkan keranjang setelah berhasil
            cartModel.setRowCount(0);
            hitungTotal();
            
        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException e) { }
            }
            JOptionPane.showMessageDialog(this, "Transaksi gagal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { }
            }
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new TransaksiFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnLogout, btnMenuDashboard, btnMenuKelolaMenu;
    private JButton btnMenuKelolaStok, btnMenuKelolaPengguna, btnMenuTransaksi, btnMenuRiwayat, btnMenuLaporan;
    private JButton btn_cetakStruk, btn_prosesBayar;
    private JToggleButton btnFilterSemua, btnFilterKopi, btnFilterNonKopi;
    private JToggleButton btnFilterMakanan, btnFilterMakananUtama;
    private JToggleButton rbCash, rbQRIS, rbDebit;
    private JLabel jLabelLogo, jLabelSubLogo, lblKeranjangTitle;
    private JLabel lblMetodeLabel, lblPajakLabel, lblTotalLabel;
    private JLabel lbl_pajak, lbl_totalHarga, lblProfile;
    private JLabel lblDashSub;
    private JPanel jPanelSidebar, jPanelMain, jPanelHeader;
    private JPanel jPanelKatalogWrapper, jPanelFilter, jPanelGrid;
    private JPanel jPanelKeranjang;
    private JScrollPane jScrollKatalog, jScrollKeranjang;
    private JTable tbl_keranjang;
    private JTextField txtSearchHeader;
    // End of variables declaration//GEN-END:variables
}
