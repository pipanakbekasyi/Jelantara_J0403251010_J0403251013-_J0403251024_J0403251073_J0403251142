-- =====================================================================
-- 0. CLEANUP / PENGAMAN (DROP IF EXISTS)
-- =====================================================================
DROP TABLE IF EXISTS notifikasi CASCADE;
DROP TABLE IF EXISTS penukaran_poin CASCADE;
DROP TABLE IF EXISTS produk_olahan CASCADE;
DROP TABLE IF EXISTS reward CASCADE;
DROP TABLE IF EXISTS penugasan CASCADE;
DROP TABLE IF EXISTS stok_minyak CASCADE;
DROP TABLE IF EXISTS penjualan CASCADE;
DROP TABLE IF EXISTS pusat CASCADE;
DROP TABLE IF EXISTS petugas CASCADE;
DROP TABLE IF EXISTS penjual CASCADE;

DROP FUNCTION IF EXISTS cek_dan_update_kapasitas_petugas() CASCADE;
DROP FUNCTION IF EXISTS auto_insert_stok_saat_selesai() CASCADE;
DROP FUNCTION IF EXISTS hitung_akumulasi_stok_minyak() CASCADE;
DROP FUNCTION IF EXISTS cek_batas_80_persen_stok() CASCADE;


-- =====================================================================
-- 1. DDL: PEMBUATAN TABEL SESUAI FITUR V12
-- =====================================================================

-- 1. TABEL PENJUAL (Dilengkapi Metode Pembayaran, Nama Penyedia Bank/E-Wallet, & No Rekening)
CREATE TABLE penjual (
    id_penjual BIGSERIAL PRIMARY KEY,
    nama_penjual VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    no_hp VARCHAR(255) NOT NULL UNIQUE,
    alamat VARCHAR(255),
    poin INTEGER NOT NULL DEFAULT 0,
    metode_pembayaran VARCHAR(50) DEFAULT 'Bank', -- 'Bank' atau 'E-Wallet'
    nama_penyedia VARCHAR(50) DEFAULT 'BCA',      -- 'BRI', 'Mandiri', 'BNI', 'BCA' / 'GoPay', 'DANA'
    no_rekening VARCHAR(255),                     -- Nomor rekening bank atau nomor HP E-Wallet
    kategori_penjual VARCHAR(100) DEFAULT 'Rumah Tangga',
    role VARCHAR(50) NOT NULL DEFAULT 'PENJUAL'
);

-- 2. TABEL PETUGAS
CREATE TABLE petugas (
    id_petugas BIGSERIAL PRIMARY KEY,
    nama_petugas VARCHAR(255) NOT NULL,
    no_telp VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL DEFAULT 'tersedia',
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    alamat VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'PETUGAS',
    beban_angkut FLOAT8 NOT NULL DEFAULT 0,
    kapasitas_maksimal FLOAT8 NOT NULL DEFAULT 200,
    tipe_aktivitas VARCHAR(50),
    CONSTRAINT chk_kapasitas_petugas CHECK (beban_angkut <= kapasitas_maksimal)
);

-- 3. TABEL PUSAT
CREATE TABLE pusat (
    id_pusat BIGSERIAL PRIMARY KEY,
    nama_gudang VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nama VARCHAR(255),
    no_telepon VARCHAR(255) UNIQUE,
    alamat VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'PUSAT'
);

-- 4. TABEL PENJUALAN (Dilengkapi Metode Pencairan Bank/E-Wallet per Transaksi)
CREATE TABLE penjualan (
    id_transaksi BIGSERIAL PRIMARY KEY,
    tanggal_transaksi DATE NOT NULL DEFAULT CURRENT_DATE,
    jumlah_minyak FLOAT8 NOT NULL,
    lokasi_penjemputan VARCHAR(255) NOT NULL,
    catatan VARCHAR(255),
    metode_pembayaran VARCHAR(50) DEFAULT 'Bank',
    nama_penyedia VARCHAR(50) DEFAULT 'BCA',
    no_rekening_tujuan VARCHAR(100),
    foto TEXT,
    keterangan_bukti TEXT,
    status VARCHAR(255) NOT NULL DEFAULT 'Transaksi Masuk',
    hasil_point FLOAT8,
    total_harga FLOAT8,
    harga_minyak FLOAT8 NOT NULL,
    penjual_id BIGINT NOT NULL REFERENCES penjual(id_penjual),
    petugas_id BIGINT REFERENCES petugas(id_petugas),
    pusat_id BIGINT DEFAULT 1 REFERENCES pusat(id_pusat),
    CONSTRAINT chk_min_15_liter CHECK (jumlah_minyak >= 15)
);

-- 5. TABEL PENUGASAN
CREATE TABLE penugasan (
    id_penugasan BIGSERIAL PRIMARY KEY,
    tanggal_penugasan DATE NOT NULL DEFAULT CURRENT_DATE,
    lokasi_penjual VARCHAR(255) NOT NULL,
    pesan_penugasan TEXT,
    status_penugasan VARCHAR(255) NOT NULL DEFAULT 'Penugasan Kurir',
    pusat_id BIGINT REFERENCES pusat(id_pusat),
    petugas_id BIGINT NOT NULL REFERENCES petugas(id_petugas),
    penjualan_id BIGINT UNIQUE REFERENCES penjualan(id_transaksi)
);

-- 6. TABEL REWARD
CREATE TABLE reward (
    id_reward BIGSERIAL PRIMARY KEY,
    nama_reward VARCHAR(255) NOT NULL,
    deskripsi VARCHAR(255),
    jumlah_poin INTEGER NOT NULL,
    penjualan_id BIGINT UNIQUE REFERENCES penjualan(id_transaksi)
);

-- 7. TABEL STOK MINYAK
CREATE TABLE stok_minyak (
    id_stok BIGSERIAL PRIMARY KEY,
    jumlah_minyak FLOAT8 NOT NULL DEFAULT 0,
    batas_stok FLOAT8 NOT NULL DEFAULT 1000,
    jumlah_update FLOAT8 NOT NULL DEFAULT 0,
    tgl_update DATE NOT NULL DEFAULT CURRENT_DATE,
    penjualan_id BIGINT REFERENCES penjualan(id_transaksi),
    pusat_id BIGINT REFERENCES pusat(id_pusat),
    CONSTRAINT chk_batas_stok_gudang CHECK (jumlah_update <= batas_stok)
);

-- 8. TABEL PRODUK OLAHAN
CREATE TABLE produk_olahan (
    id_produk BIGSERIAL PRIMARY KEY,
    nama_produk VARCHAR(255) NOT NULL,
    deskripsi VARCHAR(255),
    harga_poin FLOAT8 NOT NULL,
    stok INTEGER NOT NULL,
    gambar TEXT,
    pusat_id BIGINT REFERENCES pusat(id_pusat)
);

-- 9. TABEL PENUKARAN POIN
CREATE TABLE penukaran_poin (
    id_penukaran BIGSERIAL PRIMARY KEY,
    jumlah_poin FLOAT8 NOT NULL,
    alamat_pengantaran VARCHAR(255) NOT NULL,
    harga_poin_satuan INTEGER NOT NULL,
    tgl_penukaran DATE NOT NULL DEFAULT CURRENT_DATE,
    jumlah_poin_sisa FLOAT8 NOT NULL DEFAULT 0,
    jumlah_unit INTEGER DEFAULT 1,
    catatan VARCHAR(255),
    foto TEXT,
    keterangan_bukti TEXT,
    status VARCHAR(255) NOT NULL DEFAULT 'Request Masuk',
    penjual_id BIGINT NOT NULL REFERENCES penjual(id_penjual),
    produk_id BIGINT NOT NULL REFERENCES produk_olahan(id_produk),
    petugas_id BIGINT REFERENCES petugas(id_petugas),
    pusat_id BIGINT REFERENCES pusat(id_pusat)
);

-- 10. TABEL NOTIFIKASI
CREATE TABLE notifikasi (
    id_notifikasi BIGSERIAL PRIMARY KEY,
    judul VARCHAR(255) NOT NULL,
    pesan TEXT NOT NULL,
    tanggal DATE NOT NULL DEFAULT CURRENT_DATE,
    status_baca BOOLEAN NOT NULL DEFAULT FALSE,
    tipe VARCHAR(255) NOT NULL,
    penjual_id BIGINT REFERENCES penjual(id_penjual),
    petugas_id BIGINT REFERENCES petugas(id_petugas),
    pusat_id BIGINT REFERENCES pusat(id_pusat)
);


-- =====================================================================
-- 2. FUNCTIONS & TRIGGERS LOGIKA BISNIS
-- =====================================================================

CREATE OR REPLACE FUNCTION cek_batas_80_persen_stok()
RETURNS TRIGGER AS $$
DECLARE
    v_jumlah_update_terakhir FLOAT8 := 0;
    v_batas_stok FLOAT8 := 1000;
BEGIN
    IF NEW.jumlah_minyak < 15 THEN
        RAISE EXCEPTION 'Transaksi ditolak! Minimal penyetoran minyak jelantah adalah 15 Liter (Input: % L).', NEW.jumlah_minyak;
    END IF;

    SELECT jumlah_update, batas_stok
    INTO v_jumlah_update_terakhir, v_batas_stok
    FROM stok_minyak
    WHERE pusat_id = COALESCE(NEW.pusat_id, 1)
    ORDER BY id_stok DESC
    LIMIT 1;

    IF v_jumlah_update_terakhir >= (0.8 * v_batas_stok) THEN
        RAISE EXCEPTION 'Transaksi ditolak! Kapasitas gudang sudah mencapai batas peringatan 80%% (Stok terkumpul: % L dari batas % L).', 
                        v_jumlah_update_terakhir, v_batas_stok;
    END IF;

    IF (v_jumlah_update_terakhir + NEW.jumlah_minyak) > v_batas_stok THEN
        RAISE EXCEPTION 'Transaksi ditolak! Jumlah minyak (% L) akan membuat gudang melebihi kapasitas maksimal % L (Stok saat ini: % L).',
                        NEW.jumlah_minyak, v_batas_stok, v_jumlah_update_terakhir;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cek_stok_sebelum_transaksi
BEFORE INSERT ON penjualan
FOR EACH ROW
EXECUTE FUNCTION cek_batas_80_persen_stok();


CREATE OR REPLACE FUNCTION hitung_akumulasi_stok_minyak()
RETURNS TRIGGER AS $$
DECLARE
    v_jumlah_update_sebelumnya FLOAT8 := 0;
    v_batas_stok_sebelumnya FLOAT8 := 1000;
BEGIN
    SELECT jumlah_update, batas_stok
    INTO v_jumlah_update_sebelumnya, v_batas_stok_sebelumnya
    FROM stok_minyak
    WHERE pusat_id = COALESCE(NEW.pusat_id, 1)
    ORDER BY id_stok DESC
    LIMIT 1;

    IF FOUND THEN
        NEW.jumlah_update := v_jumlah_update_sebelumnya + NEW.jumlah_minyak;
        NEW.batas_stok := COALESCE(NEW.batas_stok, v_batas_stok_sebelumnya);
    ELSE
        NEW.jumlah_update := NEW.jumlah_minyak;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_akumulasi_stok_minyak
BEFORE INSERT ON stok_minyak
FOR EACH ROW
EXECUTE FUNCTION hitung_akumulasi_stok_minyak();


CREATE OR REPLACE FUNCTION auto_insert_stok_saat_selesai()
RETURNS TRIGGER AS $$
BEGIN
    IF UPPER(NEW.status) = 'SELESAI' AND UPPER(OLD.status) <> 'SELESAI' THEN
        INSERT INTO stok_minyak (jumlah_minyak, batas_stok, tgl_update, penjualan_id, pusat_id)
        VALUES (NEW.jumlah_minyak, 1000, CURRENT_DATE, NEW.id_transaksi, COALESCE(NEW.pusat_id, 1));

        IF NEW.petugas_id IS NOT NULL THEN
            UPDATE petugas
            SET beban_angkut = GREATEST(0, beban_angkut - NEW.jumlah_minyak),
                status = CASE 
                            WHEN GREATEST(0, beban_angkut - NEW.jumlah_minyak) < kapasitas_maksimal THEN 'tersedia'
                            ELSE status
                         END
            WHERE id_petugas = NEW.petugas_id;
        END IF;

        UPDATE penugasan
        SET status_penugasan = 'Selesai'
        WHERE penjualan_id = NEW.id_transaksi;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_selesai_penjualan_ke_stok
AFTER UPDATE OF status ON penjualan
FOR EACH ROW
EXECUTE FUNCTION auto_insert_stok_saat_selesai();


-- =====================================================================
-- 3. SEED DATA AWAL
-- =====================================================================

INSERT INTO penjual (nama_penjual, email, password, no_hp, alamat, poin, metode_pembayaran, nama_penyedia, no_rekening, kategori_penjual, role)
VALUES ('Budi Santoso', 'budi@gmail.com', 'penjual123', '081299887766', 'Jl. Melati No. 10, Bogor', 350, 'Bank', 'BCA', '7210998212', 'Rumah Tangga', 'PENJUAL');

INSERT INTO petugas (nama_petugas, no_telp, status, email, password, alamat, role, beban_angkut, kapasitas_maksimal)
VALUES ('Andi Pratama', '081234567890', 'tersedia', 'petugas@mail.com', 'petugas123', 'Jl. Merdeka No. 10', 'PETUGAS', 0, 200);

INSERT INTO pusat (nama_gudang, nama, no_telepon, email, password, alamat, role)
VALUES ('Gudang Bandung', 'Admin Gudang Bandung', '081122334455', 'pusat@mail.com', 'pusat123', 'Jl. Soekarno Hatta No. 99', 'PUSAT');

INSERT INTO stok_minyak (jumlah_minyak, batas_stok, jumlah_update, tgl_update, pusat_id)
VALUES (0, 1000, 0, CURRENT_DATE, 1);

INSERT INTO produk_olahan (nama_produk, deskripsi, harga_poin, stok, gambar, pusat_id) VALUES
('Sabun Cuci Piring', 'Sabun cuci piring cair hasil olahan minyak jelantah', 150, 20, 'https://placehold.co/300x200/FFF3B8/926008?text=Sabun+Piring', 1),
('Sabun Cuci Tangan', 'Sabun cuci tangan gel hasil olahan minyak jelantah', 120, 15, 'https://placehold.co/300x200/FFF3B8/926008?text=Sabun+Tangan', 1),
('Lilin Eco Jelantara', 'Lilin tangkai ramah lingkungan dari minyak jelantah', 200, 10, 'https://placehold.co/300x200/FFF3B8/926008?text=Lilin+Eco', 1);

INSERT INTO penjualan (tanggal_transaksi, jumlah_minyak, lokasi_penjemputan, catatan, metode_pembayaran, nama_penyedia, no_rekening_tujuan, keterangan_bukti, status, hasil_point, total_harga, harga_minyak, penjual_id, petugas_id, pusat_id)
VALUES (CURRENT_DATE, 15.0, 'Jl. Melati No. 10, Bogor', 'Minyak disimpan dalam jerigen tertutup', 'Bank', 'BCA', '7210998212', 'Menunggu penugasan kurir dari Pusat.', 'Transaksi Masuk', 150, 90000, 6000, 1, 1, 1);

INSERT INTO penukaran_poin (jumlah_poin, alamat_pengantaran, harga_poin_satuan, tgl_penukaran, jumlah_poin_sisa, jumlah_unit, catatan, keterangan_bukti, status, penjual_id, produk_id, pusat_id)
VALUES (150, 'Jl. Melati No. 10, Bogor', 150, CURRENT_DATE, 200, 1, 'Ditaruh di teras rumah', 'Permintaan redeem dibuat oleh Penjual. Menunggu konfirmasi dari Pusat.', 'Request Masuk', 1, 1, 1);

select * from petugas;