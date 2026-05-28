package dobarpos;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NavigationHelper — Navigasi sidebar + Role-Based Access Control (RBAC).
 *
 * Akses per Role (sesuai Use Case Diagram):
 *   Manager : Dashboard, Laporan (Penjualan & Stok)
 *   Admin   : Dashboard, KelolaMenu, KelolaStok, KelolaPengguna
 *   Kasir   : Transaksi, Riwayat
 */
public class NavigationHelper {

    // ── Access Matrix ─────────────────────────────────────────────
    private static final Map<String, List<String>> ACCESS = new HashMap<>();
    static {
        ACCESS.put("Manager", Arrays.asList("Laporan"));
        ACCESS.put("Admin",   Arrays.asList("Dashboard","KelolaMenu","KelolaStok","KelolaPengguna","Laporan"));
        ACCESS.put("Kasir",   Arrays.asList("Transaksi","Riwayat"));
    }

    /** Cek apakah role saat ini boleh mengakses target */
    public static boolean canAccess(String target) {
        String role = UserSession.getInstance().getRole();
        if (role == null) return false;
        List<String> allowed = ACCESS.get(role);
        return allowed != null && allowed.contains(target);
    }

    /**
     * Navigasi ke frame tujuan dengan pemeriksaan akses.
     * Jika tidak diizinkan, tampilkan pesan "Akses Ditolak".
     */
    public static void navigateTo(JFrame source, String target) {
        if (!canAccess(target)) {
            JOptionPane.showMessageDialog(source,
                "Akses ke halaman '" + target + "' tidak diizinkan untuk role: "
                + UserSession.getInstance().getRole(),
                "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFrame next = null;
        switch (target) {
            case "Dashboard":      next = new DashboardFrame();      break;
            case "KelolaMenu":     next = new KelolaMenuFrame();      break;
            case "KelolaStok":     next = new KelolaStokFrame();      break;
            case "KelolaPengguna": next = new KelolaPenggunaFrame();  break;
            case "Transaksi":      next = new TransaksiFrame();       break;
            case "Riwayat":        next = new RiwayatFrame();         break;
            case "Laporan":        next = new LaporanFrame();         break;
            default:
                JOptionPane.showMessageDialog(source,
                    "Halaman '" + target + "' belum tersedia.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
        }
        final JFrame finalNext = next;
        SwingUtilities.invokeLater(() -> {
            finalNext.setVisible(true);
            source.dispose();
        });
    }

    /**
     * Konfigurasi visibilitas tombol sidebar berdasarkan role aktif.
     * Tombol yang tidak diizinkan akan disembunyikan (setVisible(false)).
     *
     * @param buttons  Map dari nama target ke JButton sidebar.
     *                 Contoh: {"KelolaMenu", btnMenuKelolaMenu}
     */
    public static void configureSidebar(Map<String, JButton> buttons) {
        // Semua tombol sidebar selalu tampil.
        // RBAC (role check) tetap berlaku di navigateTo() —
        // tombol yang tidak diizinkan akan memunculkan dialog "Akses Ditolak" saat diklik.
        for (JButton btn : buttons.values()) {
            btn.setVisible(true);
        }
    }

    /**
     * Tandai tombol aktif di sidebar (background putih, teks merah).
     * Tombol lain dikembalikan ke warna sidebar merah tua.
     */
    public static void setActiveButton(JButton activeBtn, JButton... allBtns) {
        Color sidebarRed  = new Color(139, 29, 36);
        Color activeWhite = Color.WHITE;
        for (JButton b : allBtns) {
            if (b == null || !b.isVisible()) continue;
            if (b == activeBtn) {
                b.setBackground(activeWhite);
                b.setForeground(sidebarRed);
            } else {
                b.setBackground(sidebarRed);
                b.setForeground(activeWhite);
            }
        }
    }

    /**
     * Logout: hapus sesi, tutup koneksi DB, kembali ke LoginFrame.
     */
    public static void logout(JFrame source) {
        int konfirm = JOptionPane.showConfirmDialog(source,
            "Apakah Anda yakin ingin logout?", "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (konfirm == JOptionPane.YES_OPTION) {
            UserSession.getInstance().clearSession();
            DBConnection.closeConnection();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                source.dispose();
            });
        }
    }
}

