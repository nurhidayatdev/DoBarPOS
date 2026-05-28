import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Replacer3 {
    public static void main(String[] args) throws Exception {
        Map<String, String> rep = new HashMap<>();
        // DashboardFrame
        rep.put("TODAY'S SALES", "PENJUALAN HARI INI");
        rep.put("LOW STOCK ITEMS", "STOK MENIPIS");
        rep.put("ACTIVE STAFF", "STAF AKTIF");
        rep.put("All systems operational", "Semua sistem beroperasi normal");
        rep.put("+ New Order", "+ Pesanan Baru");
        rep.put("?  Restock", "⟲ Isi Stok"); // Note: might not match due to encoding. We'll also just match "Restock" where needed, but be careful.
        
        // KelolaMenuFrame
        rep.put("Add, update, or remove items from your POS system.", "Tambah, perbarui, atau hapus item dari sistem POS Anda.");
        rep.put("Menu Detail Form", "Form Detail Menu");
        // We'll just replace "Clear Form" -> "Bersihkan Form", the emoji will stay
        rep.put("Clear Form", "Bersihkan Form");

        // KelolaPenggunaFrame
        rep.put("Add, modify, or remove system operators and administrators.", "Tambah, modifikasi, atau hapus operator dan admin sistem.");
        // We'll replace "User Details" -> "Detail Pengguna", the emoji stays
        rep.put("User Details", "Detail Pengguna");
        // "Registered Personnel" -> "Pengguna Terdaftar", emoji stays
        rep.put("Registered Personnel", "Pengguna Terdaftar");

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
        System.out.println("Done 3.");
    }
}
