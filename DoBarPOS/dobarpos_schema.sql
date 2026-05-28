-- ============================================================
-- DoBarPOS — Database Schema
-- Jalankan script ini di phpMyAdmin atau MySQL Workbench
-- ============================================================

CREATE DATABASE IF NOT EXISTS dobarpos_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE dobarpos_db;

-- ────────────────────────────────────────────────────────────
-- TABEL: user
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user (
  id_user   INT AUTO_INCREMENT PRIMARY KEY,
  username  VARCHAR(50)  UNIQUE NOT NULL,
  password  VARCHAR(255) NOT NULL,  -- Disimpan sebagai hash SHA2-256
  role      ENUM('Manager','Admin','Kasir') NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ────────────────────────────────────────────────────────────
-- TABEL: menu
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu (
  id_menu       INT AUTO_INCREMENT PRIMARY KEY,
  nama          VARCHAR(100) NOT NULL,
  kategori      ENUM('Makanan','Minuman','Snack') NOT NULL,
  harga         DECIMAL(10,2) NOT NULL,
  is_available  TINYINT(1) DEFAULT 1,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ────────────────────────────────────────────────────────────
-- TABEL: stok_bahan
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stok_bahan (
  id_stok       INT AUTO_INCREMENT PRIMARY KEY,
  nama_bahan    VARCHAR(100) NOT NULL,
  jumlah        DECIMAL(10,2) NOT NULL DEFAULT 0,
  satuan        ENUM('Kilogram (kg)','Liter (l)','Pcs') NOT NULL,
  update_by     INT,                      -- FK ke user.id_user
  update_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (update_by) REFERENCES user(id_user) ON DELETE SET NULL
);

-- ────────────────────────────────────────────────────────────
-- TABEL: pemesanan
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pemesanan (
  id_pemesanan  INT AUTO_INCREMENT PRIMARY KEY,
  id_user       INT NOT NULL,             -- kasir yang membuat pesanan
  status        ENUM('Proses','Selesai','Dibatalkan') DEFAULT 'Proses',
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_user) REFERENCES user(id_user)
);

-- ────────────────────────────────────────────────────────────
-- TABEL: order_detail
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_detail (
  id_detail     INT AUTO_INCREMENT PRIMARY KEY,
  id_pemesanan  INT NOT NULL,
  id_menu       INT NOT NULL,
  jumlah        INT NOT NULL DEFAULT 1,
  subtotal      DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (id_pemesanan) REFERENCES pemesanan(id_pemesanan) ON DELETE CASCADE,
  FOREIGN KEY (id_menu)      REFERENCES menu(id_menu)
);

-- ────────────────────────────────────────────────────────────
-- TABEL: transaksi
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS transaksi (
  id_transaksi  INT AUTO_INCREMENT PRIMARY KEY,
  id_pemesanan  INT NOT NULL UNIQUE,
  total         DECIMAL(10,2) NOT NULL,
  pajak         DECIMAL(10,2) NOT NULL DEFAULT 0,
  status        ENUM('Paid','Void','Refund') DEFAULT 'Paid',
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_pemesanan) REFERENCES pemesanan(id_pemesanan)
);

-- ────────────────────────────────────────────────────────────
-- TABEL: pembayaran
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pembayaran (
  id_pembayaran INT AUTO_INCREMENT PRIMARY KEY,
  id_transaksi  INT NOT NULL,
  metode        ENUM('Cash','QRIS','Debit') NOT NULL,
  jumlah_bayar  DECIMAL(10,2) NOT NULL,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi)
);

-- ============================================================
-- SEED DATA (Data awal untuk testing)
-- ============================================================

-- User (password di-hash dengan SHA2-256)
INSERT INTO user (username, password, role) VALUES
  ('admin_super',      SHA2('admin123',   256), 'Admin'),
  ('yaya_manager',     SHA2('manager123', 256), 'Manager'),
  ('budi_kasir',       SHA2('kasir123',   256), 'Kasir'),
  ('siti_kasir',       SHA2('kasir123',   256), 'Kasir');

-- Menu
INSERT INTO menu (nama, kategori, harga, is_available) VALUES
  ('Caffe Latte Hot',    'Minuman', 28000, 1),
  ('Iced Americano',     'Minuman', 22000, 1),
  ('Espresso Single',    'Minuman', 18000, 1),
  ('Caramel Macchiato',  'Minuman', 32000, 1),
  ('Butter Croissant',   'Makanan', 25000, 1),
  ('Nasi Goreng Spesial','Makanan', 45000, 1),
  ('Sate Ayam Madura',   'Makanan', 60000, 1),
  ('Es Teh Manis',       'Minuman', 10000, 1);

-- Stok Bahan
INSERT INTO stok_bahan (nama_bahan, jumlah, satuan, update_by) VALUES
  ('Biji Kopi Arabica',      25.5, 'Kilogram (kg)', 1),
  ('Susu Segar (Full Cream)', 40.0, 'Liter (l)',    1),
  ('Gula Aren Cair',          15.0, 'Liter (l)',    1),
  ('Cup Plastik 16oz',        500,  'Pcs',          1);
