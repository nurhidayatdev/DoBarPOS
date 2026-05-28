import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Replacer {
    public static void main(String[] args) throws Exception {
        Map<String, String> rep = new HashMap<>();
        rep.put("\"Point of Sale Management\"", "\"Manajemen Kasir\"");
        rep.put("\"Management System\"", "\"Sistem Manajemen\"");
        rep.put("\"Dashboard Overview\"", "\"Ringkasan Dashboard\"");
        rep.put("\"Real-time metrics and system status.\"", "\"Metrik waktu nyata dan status sistem.\"");
        rep.put("\"TODAY'S SALES\"", "\"PENJUALAN HARI INI\"");
        rep.put("\"LOW STOCK ITEMS\"", "\"STOK MENIPIS\"");
        rep.put("\"ACTIVE STAFF\"", "\"STAF AKTIF\"");
        rep.put("\"Quick Actions\"", "\"Aksi Cepat\"");
        rep.put("\"New Order\"", "\"Pesanan Baru\"");
        rep.put("\"Restock\"", "\"Isi Stok\"");
        rep.put("\"System Status\"", "\"Status Sistem\"");
        rep.put("\"All systems operational\"", "\"Semua sistem beroperasi normal\"");
        rep.put("\"Order ID\"", "\"ID Pesanan\"");
        rep.put("\"Time\"", "\"Waktu\"");
        rep.put("\"Items\"", "\"Item\"");
        rep.put("\"Total\"", "\"Total\"");
        rep.put("\"Status\"", "\"Status\"");
        rep.put("\"Manage your inventory, update quantities, and track materials.\"", "\"Kelola inventaris, perbarui jumlah, dan lacak bahan baku.\"");
        rep.put("\"Review transaction history and stock movement.\"", "\"Tinjau riwayat transaksi dan pergerakan stok.\"");
        rep.put("\"Search POS data...\"", "\"Cari data POS...\"");
        rep.put("\"Search menu...\"", "\"Cari menu...\"");
        rep.put("\"Search stok...\"", "\"Cari stok...\"");
        rep.put("\"Search personnel...\"", "\"Cari staf...\"");
        rep.put("\"Search transaksi...\"", "\"Cari transaksi...\"");
        rep.put("\"Search report...\"", "\"Cari laporan...\"");
        rep.put("\"Manage system users, access levels, and staff credentials.\"", "\"Kelola pengguna sistem, tingkat akses, dan kredensial staf.\"");
        rep.put("\"Organize your food and beverage catalog.\"", "\"Atur katalog makanan dan minuman Anda.\"");
        rep.put("\"Point of Sale Management\"/>", "\"Manajemen Kasir\"/>"); // for .form

        Files.walk(Paths.get("c:/Users/ASUS/Documents/NetBeansProjects/DoBarPOS/src/dobarpos"))
            .filter(p -> p.toString().endsWith(".java") || p.toString().endsWith(".form"))
            .forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p), "UTF-8");
                    boolean mod = false;
                    for (Map.Entry<String, String> e : rep.entrySet()) {
                        if (content.contains(e.getKey())) {
                            content = content.replace(e.getKey(), e.getValue());
                            mod = true;
                        }
                    }
                    if (mod) {
                        Files.write(p, content.getBytes("UTF-8"));
                        System.out.println("Modified: " + p.getFileName());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        System.out.println("Done.");
    }
}
