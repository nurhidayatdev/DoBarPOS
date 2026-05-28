package dobarpos;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LaporanFrame — Halaman Laporan Analytics DoBarPOS.
 *
 * Saran Library PDF Export:
 *   → iText 5 (itextpdf-5.x.jar) — paling umum digunakan, mudah dipelajari.
 *     Download: https://github.com/itext/itextpdf/releases
 *   → Apache PDFBox — 100% open source, cocok untuk proyek non-komersial.
 *     Download: https://pdfbox.apache.org/download.html
 *   Tambahkan salah satu .jar ke Libraries di NetBeans (klik kanan project → Properties → Libraries → Add JAR).
 *
 * Filter Tanggal:
 *   Menggunakan JTextField biasa (lebih portabel).
 *   Alternatif: JDateChooser dari JCalendar.jar (lebih user-friendly).
 */
public class LaporanFrame extends JFrame {

    private DefaultTableModel penjualanModel, stokModel;
    private JLabel lblTotalPendapatan, lblTotalTransaksi, lblMenuTerlaris, lblMenuTerlarisSub;

    public LaporanFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadDataPenjualan();
        loadDataStok();

        // Navigasi Sidebar
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
        NavbarHelper.setupFullNavbar(jPanelHeader, txtSearch, lblProfile, btnLogout, "Cari laporan periode...");

        // Search Logic
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            private void filterTable() {
                String text = txtSearch.getText();
                javax.swing.table.TableRowSorter<?> sorter = new javax.swing.table.TableRowSorter<>(penjualanModel);
                tbl_laporanPenjualan.setRowSorter(sorter);
                if (text.equals("Cari laporan periode...") || text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
    }

    private void loadDataPenjualan() {
        penjualanModel.setRowCount(0);
        
        String sqlTable = "SELECT DATE_FORMAT(t.created_at,'%d %b %Y, %H:%i') AS tanggal, "
                        + "t.id_transaksi, u.username AS kasir, "
                        + "COALESCE(SUM(od.jumlah), 0) AS total_item, t.total, t.status "
                        + "FROM transaksi t "
                        + "JOIN pemesanan p ON t.id_pemesanan = p.id_pemesanan "
                        + "JOIN user u ON p.id_user = u.id_user "
                        + "LEFT JOIN order_detail od ON od.id_pemesanan = p.id_pemesanan "
                        + "GROUP BY t.id_transaksi "
                        + "ORDER BY t.created_at DESC";

        String sqlStats = "SELECT COALESCE(SUM(total), 0) AS total_pendapatan, COUNT(*) AS total_transaksi "
                        + "FROM transaksi "
                        + "WHERE status != 'Refund' AND status != 'Void'";

        String sqlBest = "SELECT m.nama, SUM(od.jumlah) AS total_terjual "
                       + "FROM order_detail od "
                       + "JOIN menu m ON od.id_menu = m.id_menu "
                       + "JOIN pemesanan p ON od.id_pemesanan = p.id_pemesanan "
                       + "JOIN transaksi t ON t.id_pemesanan = p.id_pemesanan "
                       + "WHERE t.status != 'Refund' AND t.status != 'Void' "
                       + "GROUP BY m.id_menu "
                       + "ORDER BY total_terjual DESC "
                       + "LIMIT 1";

        try (Connection conn = DBConnection.getConnection()) {
            
            // 1) Load data tabel penjualan
            try (PreparedStatement ps = conn.prepareStatement(sqlTable);
                 ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    penjualanModel.addRow(new Object[]{
                        rs.getString("tanggal"),
                        "#TRX-" + rs.getInt("id_transaksi"),
                        rs.getString("kasir"),
                        rs.getInt("total_item"),
                        formatRp((int)rs.getDouble("total")),
                        rs.getString("status")
                    });
                }
            }
            
            // 2) Load Summary: Total Pendapatan & Transaksi
            try (PreparedStatement ps = conn.prepareStatement(sqlStats);
                 ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    lblTotalPendapatan.setText(formatRp((int)rs.getDouble("total_pendapatan")));
                    lblTotalTransaksi.setText(String.valueOf(rs.getInt("total_transaksi")));
                }
            }
            
            // 3) Load Summary: Menu Terlaris
            lblMenuTerlaris.setText("-");
            lblMenuTerlarisSub.setText("0 porsi terjual");
            try (PreparedStatement ps = conn.prepareStatement(sqlBest);
                 ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    lblMenuTerlaris.setText(rs.getString("nama"));
                    lblMenuTerlarisSub.setText(rs.getInt("total_terjual") + " porsi terjual");
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data penjualan: " + e.getMessage());
        }
    }

    private void loadDataStok() {
        stokModel.setRowCount(0);
        String sql = "SELECT nama_bahan, jumlah, satuan, "
                   + "DATE_FORMAT(update_at,'%d %b %Y, %H:%i') AS terakhir_update "
                   + "FROM stok_bahan "
                   + "ORDER BY nama_bahan ASC";
                   
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while(rs.next()) {
                stokModel.addRow(new Object[]{
                    rs.getString("nama_bahan"),
                    rs.getDouble("jumlah"),
                    rs.getString("satuan"),
                    rs.getString("terakhir_update") != null ? rs.getString("terakhir_update") : "-"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data stok: " + e.getMessage());
        }
    }

    private String formatRp(int n) {
        return "Rp " + NumberFormat.getInstance(new Locale("id","ID")).format(n);
    }

    private JPanel buatSummaryCard(String judul, Color bgColor) {
        JPanel card = new JPanel(null);
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createLineBorder(new Color(220,210,210)));
        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblJudul.setForeground(new Color(120,80,80));
        lblJudul.setBounds(16, 14, 200, 16);
        card.add(lblJudul);
        return card;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelSidebar    = new JPanel();
        jLabelLogo       = new JLabel();
        jLabelSubLogo    = new JLabel();
        btnMenuDashboard = new JButton(); btnMenuKelolaMenu = new JButton();
        btnMenuKelolaStok = new JButton(); btnMenuKelolaPengguna = new JButton();
        btnMenuTransaksi = new JButton(); btnMenuRiwayat = new JButton();
        btnMenuLaporan   = new JButton();

        jPanelMain    = new JPanel();
        jPanelHeader  = new JPanel();
        txtSearch     = new JTextField();
        lblProfile    = new JLabel();
        btnLogout     = new JButton();

        jPanelContent   = new JPanel();
        jPanelPageHeader= new JPanel();
        lblTitle        = new JLabel();
        lblSubTitle     = new JLabel();
        txtStartDate    = new JTextField();
        txtEndDate      = new JTextField();
        btn_generatePDF = new JButton();

        jTabbedPane     = new JTabbedPane();

        // Tab Penjualan
        jPanelPenjualan = new JPanel();
        jPanelCards     = new JPanel();
        lblTotalPendapatan  = new JLabel("Rp 0");
        lblTotalTransaksi   = new JLabel("0");
        lblMenuTerlaris     = new JLabel("-");
        lblMenuTerlarisSub  = new JLabel("");
        jScrollPenjualan    = new JScrollPane();
        tbl_laporanPenjualan= new JTable();

        // Tab Stok
        jPanelStok      = new JPanel();
        jScrollStok     = new JScrollPane();
        tbl_laporanStok = new JTable();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DoBarPOS - Laporan Analytics");
        getContentPane().setLayout(new BorderLayout());

        // ── SIDEBAR ──────────────────────────────────────────────
        jPanelSidebar.setBackground(new Color(139,29,36));
        jPanelSidebar.setPreferredSize(new Dimension(250,700));
        jPanelSidebar.setLayout(null);

        jLabelLogo.setFont(new Font("SansSerif",Font.BOLD,22));
        jLabelLogo.setForeground(Color.WHITE);
        jLabelLogo.setText("DoBarPOS");
        jPanelSidebar.add(jLabelLogo);
        jLabelLogo.setBounds(18,28,130,30);

        jLabelSubLogo.setFont(new Font("SansSerif",Font.PLAIN,11));
        jLabelSubLogo.setForeground(new Color(220,220,220));
        jLabelSubLogo.setText("Sistem Manajemen");
        jPanelSidebar.add(jLabelSubLogo);
        jLabelSubLogo.setBounds(18,56,140,16);

        String[] sLbls = {"⊞  Dashboard","🍴  Kelola Menu","📦  Kelola Stok","👥  Kelola Pengguna","📝  Transaksi","🕐  Riwayat","📊  Laporan"};
        JButton[] sBtns = {btnMenuDashboard,btnMenuKelolaMenu,btnMenuKelolaStok,btnMenuKelolaPengguna,btnMenuTransaksi,btnMenuRiwayat,btnMenuLaporan};
        for (int i=0;i<sBtns.length;i++) {
            sBtns[i].setBackground(i==6?Color.WHITE:new Color(139,29,36));
            sBtns[i].setForeground(i==6?new Color(139,29,36):Color.WHITE);
            sBtns[i].setFont(new Font("SansSerif",Font.BOLD,12));
            sBtns[i].setText(sLbls[i]);
            sBtns[i].setHorizontalAlignment(SwingConstants.LEFT);
            jPanelSidebar.add(sBtns[i]);
            sBtns[i].setBounds(10, 95 + i * 46, 230, 40);
        }
        getContentPane().add(jPanelSidebar,BorderLayout.WEST);

        // ── HEADER ───────────────────────────────────────────────
        jPanelMain.setBackground(new Color(252,246,246));
        jPanelMain.setLayout(new BorderLayout());

        jPanelHeader.setBackground(Color.WHITE);
        jPanelHeader.setPreferredSize(new Dimension(900,60));
        jPanelHeader.setLayout(new BorderLayout());

        JPanel hL=new JPanel(new FlowLayout(FlowLayout.LEFT,16,10));
        hL.setOpaque(false);
        txtSearch.setText("Cari laporan...");
        txtSearch.setPreferredSize(new Dimension(240,38));
        hL.add(txtSearch);
        jPanelHeader.add(hL,BorderLayout.WEST);

        JPanel hR=new JPanel(new FlowLayout(FlowLayout.RIGHT,16,10));
        hR.setOpaque(false);
        lblProfile.setFont(new Font("SansSerif",Font.BOLD,13));
        lblProfile.setText("Yaya - Manager");
        btnLogout.setText("Logout");
        btnLogout.setPreferredSize(new Dimension(80,36));
        hR.add(lblProfile); hR.add(btnLogout);
        jPanelHeader.add(hR,BorderLayout.EAST);
        jPanelMain.add(jPanelHeader,BorderLayout.NORTH);

        // ── CONTENT ──────────────────────────────────────────────
        jPanelContent.setBackground(new Color(252,246,246));
        jPanelContent.setLayout(new BorderLayout(0,10));
        jPanelContent.setBorder(BorderFactory.createEmptyBorder(10,30,30,30));

        // Page Header (Judul + Filter + Tombol)
        jPanelPageHeader.setOpaque(false);
        jPanelPageHeader.setLayout(new BorderLayout(10,0));

        JPanel phLeft = new JPanel(new FlowLayout(FlowLayout.LEFT,0,2));
        phLeft.setOpaque(false);
        lblTitle.setFont(new Font("SansSerif",Font.BOLD,26));
        lblTitle.setText("Laporan Analytics");
        lblSubTitle.setFont(new Font("SansSerif",Font.PLAIN,13));
        lblSubTitle.setForeground(new Color(130,130,130));
        lblSubTitle.setText("Tinjau riwayat transaksi dan pergerakan stok.");
        JPanel titlesCol=new JPanel(new GridLayout(2,1));
        titlesCol.setOpaque(false);
        titlesCol.add(lblTitle); titlesCol.add(lblSubTitle);
        phLeft.add(titlesCol);
        jPanelPageHeader.add(phLeft,BorderLayout.WEST);

        // Filter tanggal + tombol di sebelah kanan judul
        // Pilihan: JTextField biasa (digunakan di sini karena tidak perlu library tambahan)
        // Alternatif premium: ganti dengan JDateChooser dari JCalendar.jar
        JPanel phRight=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        phRight.setOpaque(false);
        txtStartDate.setText("📅 01 Mei 2026");
        txtStartDate.setFont(new Font("SansSerif",Font.PLAIN,12));
        txtStartDate.setPreferredSize(new Dimension(130,34));
        txtEndDate.setText("📅 31 Mei 2026");
        txtEndDate.setFont(new Font("SansSerif",Font.PLAIN,12));
        txtEndDate.setPreferredSize(new Dimension(130,34));

        btn_generatePDF.setText("📄 Generate PDF");
        btn_generatePDF.setFont(new Font("SansSerif",Font.BOLD,12));
        btn_generatePDF.setBackground(new Color(139,29,36));
        btn_generatePDF.setForeground(Color.WHITE);
        btn_generatePDF.setPreferredSize(new Dimension(140,34));
        btn_generatePDF.addActionListener(e->btn_generatePDFActionPerformed());

        phRight.add(txtStartDate); phRight.add(txtEndDate);
        phRight.add(btn_generatePDF);
        jPanelPageHeader.add(phRight,BorderLayout.EAST);

        jPanelContent.add(jPanelPageHeader,BorderLayout.NORTH);

        // ── TABBED PANE ──────────────────────────────────────────
        jTabbedPane.setFont(new Font("SansSerif",Font.BOLD,13));
        jTabbedPane.setBackground(Color.WHITE);

        // ── TAB 1: LAPORAN PENJUALAN ─────────────────────────────
        jPanelPenjualan.setBackground(Color.WHITE);
        jPanelPenjualan.setLayout(new BorderLayout(0,12));
        jPanelPenjualan.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        // Summary Cards (3 kolom)
        jPanelCards.setOpaque(false);
        jPanelCards.setLayout(new GridLayout(1,3,14,0));
        jPanelCards.setPreferredSize(new Dimension(900,110));

        // Card 1 — Total Pendapatan
        JPanel card1 = buatSummaryCard("TOTAL PENDAPATAN", new Color(252,245,245));
        lblTotalPendapatan.setFont(new Font("SansSerif",Font.BOLD,26));
        lblTotalPendapatan.setForeground(new Color(30,30,30));
        lblTotalPendapatan.setBounds(16,34,280,36);
        card1.add(lblTotalPendapatan);
        jPanelCards.add(card1);

        // Card 2 — Total Transaksi
        JPanel card2 = buatSummaryCard("TOTAL TRANSAKSI", new Color(252,245,245));
        lblTotalTransaksi.setFont(new Font("SansSerif",Font.BOLD,32));
        lblTotalTransaksi.setForeground(new Color(30,30,30));
        lblTotalTransaksi.setBounds(16,34,200,42);
        card2.add(lblTotalTransaksi);
        jPanelCards.add(card2);

        // Card 3 — Menu Terlaris
        JPanel card3 = buatSummaryCard("MENU TERLARIS", new Color(252,245,245));
        lblMenuTerlaris.setFont(new Font("SansSerif",Font.BOLD,18));
        lblMenuTerlaris.setForeground(new Color(30,30,30));
        lblMenuTerlaris.setBounds(16,34,280,26);
        card3.add(lblMenuTerlaris);
        lblMenuTerlarisSub.setFont(new Font("SansSerif",Font.PLAIN,12));
        lblMenuTerlarisSub.setForeground(new Color(130,130,130));
        lblMenuTerlarisSub.setBounds(16,62,280,18);
        card3.add(lblMenuTerlarisSub);
        jPanelCards.add(card3);

        jPanelPenjualan.add(jPanelCards,BorderLayout.NORTH);

        // Tabel Penjualan
        penjualanModel = new DefaultTableModel(
            new String[]{"TANGGAL","NO. TRANSAKSI","KASIR","TOTAL ITEM","TOTAL HARGA","STATUS"},0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        tbl_laporanPenjualan.setModel(penjualanModel);
        tbl_laporanPenjualan.setRowHeight(38);
        tbl_laporanPenjualan.setShowGrid(false);
        tbl_laporanPenjualan.setSelectionBackground(new Color(252,230,230));
        tbl_laporanPenjualan.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,11));
        tbl_laporanPenjualan.getTableHeader().setBackground(new Color(252,246,246));
        tbl_laporanPenjualan.getColumnModel().getColumn(0).setPreferredWidth(170);
        tbl_laporanPenjualan.getColumnModel().getColumn(1).setPreferredWidth(130);
        jScrollPenjualan.setViewportView(tbl_laporanPenjualan);
        jScrollPenjualan.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
        jPanelPenjualan.add(jScrollPenjualan,BorderLayout.CENTER);

        jTabbedPane.addTab("Laporan Penjualan",jPanelPenjualan);

        // ── TAB 2: LAPORAN STOK ──────────────────────────────────
        jPanelStok.setBackground(Color.WHITE);
        jPanelStok.setLayout(new BorderLayout());
        jPanelStok.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        stokModel = new DefaultTableModel(
            new String[]{"NAMA BAHAN","JUMLAH","SATUAN","TERAKHIR DIUPDATE"},0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        tbl_laporanStok.setModel(stokModel);
        tbl_laporanStok.setRowHeight(38);
        tbl_laporanStok.setShowGrid(false);
        tbl_laporanStok.setSelectionBackground(new Color(252,230,230));
        tbl_laporanStok.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,11));
        tbl_laporanStok.getTableHeader().setBackground(new Color(252,246,246));
        tbl_laporanStok.getColumnModel().getColumn(0).setPreferredWidth(240);
        tbl_laporanStok.getColumnModel().getColumn(3).setPreferredWidth(180);
        jScrollStok.setViewportView(tbl_laporanStok);
        jScrollStok.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
        jPanelStok.add(jScrollStok,BorderLayout.CENTER);

        jTabbedPane.addTab("Laporan Stok",jPanelStok);

        jPanelContent.add(jTabbedPane,BorderLayout.CENTER);
        jPanelMain.add(jPanelContent,BorderLayout.CENTER);
        getContentPane().add(jPanelMain,BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // ── Event Handlers ────────────────────────────────────────────


    private void btn_generatePDFActionPerformed() {
        try {
            // Pilih lokasi simpan
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan PDF");
            fileChooser.setSelectedFile(new File("Laporan_DoBarPOS_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String path = fileToSave.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".pdf")) {
                    path += ".pdf";
                }
                
                Document doc = new Document(PageSize.A4.rotate()); // Landscape
                PdfWriter.getInstance(doc, new FileOutputStream(path));
                doc.open();
                
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                
                doc.add(new Paragraph("LAPORAN ANALYTICS - DoBarPOS", titleFont));
                doc.add(new Paragraph("Periode: " + txtStartDate.getText() + " s/d " + txtEndDate.getText(), normalFont));
                doc.add(new Paragraph("Dicetak pada: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date()), normalFont));
                doc.add(Chunk.NEWLINE);
                
                // Pilih tabel mana yang sedang aktif
                boolean isPenjualan = jTabbedPane.getSelectedIndex() == 0;
                DefaultTableModel activeModel = isPenjualan ? penjualanModel : stokModel;
                
                PdfPTable pdfTable = new PdfPTable(activeModel.getColumnCount());
                pdfTable.setWidthPercentage(100);
                
                // Header
                com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
                BaseColor themeRed = new BaseColor(139, 29, 36);
                
                for (int i=0; i<activeModel.getColumnCount(); i++) {
                    PdfPCell headerCell = new PdfPCell(new Phrase(activeModel.getColumnName(i), headerFont));
                    headerCell.setBackgroundColor(themeRed);
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    headerCell.setPadding(8);
                    headerCell.setBorderColor(BaseColor.WHITE);
                    pdfTable.addCell(headerCell);
                }
                
                // Data
                for (int r=0; r<activeModel.getRowCount(); r++) {
                    for (int c=0; c<activeModel.getColumnCount(); c++) {
                        Object val = activeModel.getValueAt(r, c);
                        PdfPCell dataCell = new PdfPCell(new Phrase(val != null ? val.toString() : "", FontFactory.getFont(FontFactory.HELVETICA, 10)));
                        dataCell.setPadding(4);
                        pdfTable.addCell(dataCell);
                    }
                }
                
                doc.add(pdfTable);
                doc.close();
                JOptionPane.showMessageDialog(this, "Laporan PDF berhasil disimpan ke:\n" + path, "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(()->new LaporanFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnLogout,btnMenuDashboard,btnMenuKelolaMenu,btnMenuKelolaStok;
    private JButton btnMenuKelolaPengguna,btnMenuTransaksi,btnMenuRiwayat,btnMenuLaporan;
    private JButton btn_generatePDF;
    private JLabel jLabelLogo,jLabelSubLogo,lblProfile,lblTitle,lblSubTitle;
    private JPanel jPanelSidebar,jPanelMain,jPanelHeader,jPanelContent;
    private JPanel jPanelPageHeader,jPanelCards;
    private JPanel jPanelPenjualan,jPanelStok;
    private JScrollPane jScrollPenjualan,jScrollStok;
    private JTable tbl_laporanPenjualan,tbl_laporanStok;
    private JTabbedPane jTabbedPane;
    private JTextField txtSearch,txtStartDate,txtEndDate;
    // End of variables declaration//GEN-END:variables
}
