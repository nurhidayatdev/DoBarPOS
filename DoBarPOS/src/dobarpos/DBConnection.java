package dobarpos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection — Kelas Singleton untuk manajemen koneksi MySQL.
 *
 * SETUP SEBELUM DIGUNAKAN:
 * 1. Unduh MySQL Connector/J (mysql-connector-j-9.x.x.jar) dari:
 *    https://dev.mysql.com/downloads/connector/j/
 *    Pilih "Platform Independent" → download .zip → ambil file .jar di dalamnya.
 *
 * 2. Tambahkan JAR ke NetBeans:
 *    Klik kanan project → Properties → Libraries → Compile tab
 *    → Klik tombol "Add JAR/Folder" → Pilih mysql-connector-j-*.jar dari folder lib/
 *
 * 3. Sesuaikan konstanta DB_NAME, DB_USER, DB_PASS di bawah.
 *
 * 4. Pastikan MySQL server sudah berjalan (misal: via XAMPP → Start MySQL).
 *
 * SKEMA DATABASE (jalankan di MySQL/phpMyAdmin):
 * CREATE DATABASE dobarpos_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 * USE dobarpos_db;
 * -- Tabel user
 * CREATE TABLE user (
 *   id_user   INT AUTO_INCREMENT PRIMARY KEY,
 *   username  VARCHAR(50) UNIQUE NOT NULL,
 *   password  VARCHAR(255) NOT NULL,  -- simpan hash SHA-256 / bcrypt
 *   role      ENUM('Manager','Admin','Kasir') NOT NULL
 * );
 * -- Seed data awal
 * INSERT INTO user (username, password, role) VALUES
 *   ('admin_super', SHA2('admin123', 256), 'Admin'),
 *   ('yaya_manager', SHA2('manager123', 256), 'Manager'),
 *   ('budi_kasir', SHA2('kasir123', 256), 'Kasir');
 */
public class DBConnection {

    // ── Konfigurasi Koneksi ──────────────────────────────────────
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "dobarpos_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";          // kosong jika pakai XAMPP default

    private static final String JDBC_URL =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
        + "?useSSL=false&serverTimezone=Asia/Makassar&allowPublicKeyRetrieval=true";

    private static Connection connection = null;

    // ── Singleton: getConnection() ───────────────────────────────
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
                System.out.println("[DBConnection] Koneksi ke MySQL berhasil!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] Driver MySQL tidak ditemukan! Tambahkan mysql-connector-j.jar ke Libraries.");
            javax.swing.JOptionPane.showMessageDialog(null,
                "Driver MySQL tidak ditemukan!\nTambahkan mysql-connector-j.jar ke project Libraries.",
                "Error Koneksi", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Gagal konek ke MySQL: " + e.getMessage());
            javax.swing.JOptionPane.showMessageDialog(null,
                "Gagal terhubung ke database MySQL!\n\n"
                + "Pastikan:\n"
                + "  1. MySQL server sudah berjalan (XAMPP → Start MySQL)\n"
                + "  2. Database '" + DB_NAME + "' sudah dibuat\n"
                + "  3. Username & password benar\n\n"
                + "Detail: " + e.getMessage(),
                "Error Koneksi Database", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }

    /** Menutup koneksi saat aplikasi ditutup */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DBConnection] Koneksi ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Gagal menutup koneksi: " + e.getMessage());
        }
    }

    /** Test koneksi — panggil dari main untuk debug */
    public static boolean testConnection() {
        Connection conn = getConnection();
        return conn != null;
    }
}
