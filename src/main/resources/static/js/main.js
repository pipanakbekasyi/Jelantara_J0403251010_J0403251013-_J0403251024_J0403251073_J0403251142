// Menampilkan pop-up SweetAlert2 otomatis jika ada flash message dari Controller
document.addEventListener("DOMContentLoaded", function () {
    const flashSuccess = document.body.getAttribute("data-flash-success");
    const flashError = document.body.getAttribute("data-flash-error");
    const flashWarning = document.body.getAttribute("data-flash-warning");

    if (flashSuccess && flashSuccess.trim() !== "") {
        if ("Notification" in window && Notification.permission === "granted") {
            try { new Notification("Update Jelantara", { body: flashSuccess }); } catch (e) {}
        }
        Swal.fire({
            icon: 'success',
            title: 'Berhasil!',
            text: flashSuccess,
            confirmButtonText: 'Tutup',
            confirmButtonColor: '#FFCA00',
            customClass: { confirmButton: 'text-dark fw-bold rounded-pill px-4' }
        });
    }
    if (flashError && flashError.trim() !== "") {
        Swal.fire({
            icon: 'error',
            title: 'Perhatian',
            text: flashError,
            confirmButtonText: 'Mengerti',
            confirmButtonColor: '#F7B32E',
            customClass: { confirmButton: 'text-dark fw-bold rounded-pill px-4' }
        });
    }
    if (flashWarning && flashWarning.trim() !== "") {
        Swal.fire({
            icon: 'warning',
            title: 'Peringatan Kapasitas!',
            text: flashWarning,
            confirmButtonText: 'Siapkan Pengiriman',
            confirmButtonColor: '#FFCA00',
            customClass: { confirmButton: 'text-dark fw-bold rounded-pill px-4' }
        });
    }

    if (window.lucide) {
        lucide.createIcons();
    }
    hitungEstimasiOrder();
    hitungEstimasiRedeemForm();
});

// Menghitung estimasi otomatis pendapatan Rupiah dan Poin pada Form Order Penjual (Minimal 15L)
function hitungEstimasiOrder() {
    const literInput = document.getElementById("inputJumlahMinyak");
    const hargaInput = document.getElementById("hargaAcuanLiter");
    if (!literInput || !hargaInput) return;

    const liter = parseFloat(literInput.value) || 0;
    const harga = parseFloat(hargaInput.value) || 6000;
    const totalUang = Math.round(liter * harga);
    const totalPoin = Math.round(liter * 10);

    const elRp = document.getElementById("estimasiRupiah");
    const elPoin = document.getElementById("estimasiPoin");
    if (elRp) elRp.innerText = "Rp " + totalUang.toLocaleString("id-ID");
    if (elPoin) elPoin.innerText = "+" + totalPoin + " Poin";
}

// Menghitung estimasi poin yang dibutuhkan pada Form Tab Redeem di POV Penjual
function hitungEstimasiRedeemForm() {
    const selProduk = document.getElementById("selectProdukRedeem");
    const qtyInput = document.getElementById("inputQtyRedeem");
    const totalPoinEl = document.getElementById("estimasiPoinRedeem");
    if (!selProduk || !qtyInput || !totalPoinEl) return;

    const selectedOpt = selProduk.options[selProduk.selectedIndex];
    const poinSatuan = parseFloat(selectedOpt ? selectedOpt.getAttribute("data-poin") : 0) || 0;
    const qty = parseInt(qtyInput.value, 10) || 1;
    totalPoinEl.innerText = (poinSatuan * qty).toLocaleString("id-ID") + " Poin";
}

// Membuka Slide-out Side Panel untuk menampilkan Detail Lengkap Transaksi Jual Minyak
function bukaDetailTransaksi(buttonEl) {
    if (!buttonEl) return;
    const id = buttonEl.getAttribute("data-id");
    const elResi = document.getElementById("spResi");
    if (elResi) elResi.innerText = "JLT-" + String(id).padStart(6, '0');
    
    setSpText("spJenis", "Jual Minyak");
    setSpText("spTanggal", buttonEl.getAttribute("data-tanggal"));
    setSpText("spPenjual", buttonEl.getAttribute("data-penjual") || "-");
    setSpText("spPetugas", buttonEl.getAttribute("data-petugas") || "Belum Ditugaskan");
    setSpText("spLiter", (buttonEl.getAttribute("data-liter") || "0") + " Liter Minyak");
    
    const harga = parseFloat(buttonEl.getAttribute("data-harga") || 0);
    const total = parseFloat(buttonEl.getAttribute("data-total") || 0);
    const poin = buttonEl.getAttribute("data-poin") || "0";
    setSpText("spTotal", "Rp " + total.toLocaleString("id-ID") + " (+" + poin + " Poin)");
    
    setSpText("spLokasi", buttonEl.getAttribute("data-lokasi"));
    setSpText("spCatatan", buttonEl.getAttribute("data-catatan") || "-");
    setSpText("spStatus", buttonEl.getAttribute("data-status"));
    setSpText("spKetBukti", buttonEl.getAttribute("data-ketbukti") || "Belum ada catatan bukti update.");

    tampilkanFotoSidePanel(buttonEl.getAttribute("data-foto"));
    bukaSidePanel();
}

// Membuka Slide-out Side Panel untuk Riwayat Transaksi Terpadu (Jual Minyak & Redeem Poin)
function bukaDetailTransaksiUnified(rowEl) {
    if (!rowEl) return;
    const jenis = rowEl.getAttribute("data-jenis");
    const jenisLabel = (jenis === "REDEEM") ? "Redeem Poin Reward" : "Jual Minyak Jelantah";
    
    setSpText("spJenis", jenisLabel);
    setSpText("spResi", rowEl.getAttribute("data-resi"));
    setSpText("spTanggal", rowEl.getAttribute("data-tanggal"));
    setSpText("spPenjual", rowEl.getAttribute("data-penjual") || "-");
    setSpText("spPetugas", rowEl.getAttribute("data-petugas") || "Belum Ditugaskan");
    setSpText("spItem", rowEl.getAttribute("data-item"));
    setSpText("spLiter", rowEl.getAttribute("data-item"));
    
    const total = rowEl.getAttribute("data-total");
    const poin = rowEl.getAttribute("data-poin");
    if (total && total !== "null" && total !== "") {
        setSpText("spTotal", "Rp " + parseFloat(total).toLocaleString("id-ID") + " (+" + poin + " Poin)");
    } else {
        setSpText("spTotal", Math.abs(parseFloat(poin || 0)).toLocaleString("id-ID") + " Poin Digunakan");
    }
    
    setSpText("spLokasi", rowEl.getAttribute("data-alamat"));
    setSpText("spCatatan", rowEl.getAttribute("data-catatan") || "-");
    setSpText("spStatus", rowEl.getAttribute("data-status"));
    setSpText("spKetBukti", rowEl.getAttribute("data-ketbukti") || "Belum ada catatan bukti update.");

    tampilkanFotoSidePanel(rowEl.getAttribute("data-foto"));
    bukaSidePanel();
}

// Membuka Slide-out Side Panel untuk Detail Redeem Poin (POV Pusat & Petugas)
function bukaDetailRedeem(btnEl) {
    if (!btnEl) return;
    setSpText("spJenis", "Redeem Poin Reward");
    setSpText("spResi", btnEl.getAttribute("data-resi"));
    setSpText("spTanggal", btnEl.getAttribute("data-tanggal"));
    setSpText("spPenjual", btnEl.getAttribute("data-penjual") || "-");
    setSpText("spPetugas", btnEl.getAttribute("data-petugas") || "Belum Ditugaskan");
    setSpText("spLiter", btnEl.getAttribute("data-item"));
    setSpText("spItem", btnEl.getAttribute("data-item"));
    setSpText("spTotal", btnEl.getAttribute("data-poin") + " Poin");
    setSpText("spLokasi", btnEl.getAttribute("data-alamat"));
    setSpText("spCatatan", btnEl.getAttribute("data-catatan") || "-");
    setSpText("spStatus", btnEl.getAttribute("data-status"));
    setSpText("spKetBukti", btnEl.getAttribute("data-ketbukti") || "Belum ada bukti update.");

    tampilkanFotoSidePanel(btnEl.getAttribute("data-foto"));
    bukaSidePanel();
}

function setSpText(elemId, text) {
    const el = document.getElementById(elemId);
    if (el) el.innerText = text || "-";
}

function tampilkanFotoSidePanel(fotoUrl) {
    const imgEl = document.getElementById("spFotoBukti");
    const noImgEl = document.getElementById("spNoFoto");
    if (!imgEl) return;

    if (fotoUrl && fotoUrl.trim() !== "" && fotoUrl !== "null") {
        imgEl.src = fotoUrl;
        imgEl.classList.remove("d-none");
        if (noImgEl) noImgEl.classList.add("d-none");
    } else {
        imgEl.classList.add("d-none");
        if (noImgEl) noImgEl.classList.remove("d-none");
    }
}

function bukaSidePanel() {
    const drawer = document.getElementById("sidePanelDrawer");
    const backdrop = document.getElementById("sidePanelBackdrop");
    if (drawer) drawer.classList.add("open");
    if (backdrop) backdrop.classList.add("show");
}

function tutupDetailTransaksi() {
    const drawer = document.getElementById("sidePanelDrawer");
    const backdrop = document.getElementById("sidePanelBackdrop");
    if (drawer) drawer.classList.remove("open");
    if (backdrop) backdrop.classList.remove("show");
}

// Membuka Modal Popup Bukti Update Status Jual Minyak untuk Petugas & Pusat
function bukaModalUpdateBukti(btnEl) {
    const id = btnEl.getAttribute("data-id");
    const elId = document.getElementById("modalUpdateIdTrx");
    if (elId) elId.value = id;
    const elResi = document.getElementById("modalUpdateResiLabel");
    if (elResi) elResi.innerText = "JLT-" + String(id).padStart(6, '0');
    const elLiter = document.getElementById("modalUpdateLiter");
    if (elLiter) elLiter.value = btnEl.getAttribute("data-liter") || "15";
    const elStatus = document.getElementById("modalUpdateStatus");
    if (elStatus) elStatus.value = btnEl.getAttribute("data-status");
    const modalEl = document.getElementById("modalBuktiUpdate");
    if (modalEl) {
        const modal = new bootstrap.Modal(modalEl);
        modal.show();
    }
}

// Membuka Modal Popup Bukti Update Status Redeem untuk Pusat
function bukaModalUpdateRedeem(btnEl) {
    const id = btnEl.getAttribute("data-id");
    const resi = btnEl.getAttribute("data-resi") || ("RDM-" + String(id).padStart(6, '0'));
    const elId = document.getElementById("modalRedeemId");
    if (elId) elId.value = id;
    const elLabel = document.getElementById("modalRedeemResiLabel");
    if (elLabel) elLabel.innerText = resi;
    const elStatus = document.getElementById("modalRedeemStatusSelect");
    if (elStatus) elStatus.value = btnEl.getAttribute("data-status");
    const modalEl = document.getElementById("modalRedeemUpdate");
    if (modalEl) {
        const modal = new bootstrap.Modal(modalEl);
        modal.show();
    }
}

// Membuka Modal Popup Bukti Update Status Redeem untuk Petugas
function bukaModalUpdateRedeemPetugas(btnEl) {
    const id = btnEl.getAttribute("data-id");
    const resi = btnEl.getAttribute("data-resi") || ("RDM-" + String(id).padStart(6, '0'));
    const elId = document.getElementById("modalPetugasRedeemId");
    if (elId) elId.value = id;
    const elLabel = document.getElementById("modalPetugasRedeemResiLabel");
    if (elLabel) elLabel.innerText = resi;
    const elStatus = document.getElementById("modalPetugasRedeemStatus");
    if (elStatus) elStatus.value = btnEl.getAttribute("data-status");
    const modalEl = document.getElementById("modalRedeemUpdatePetugas");
    if (modalEl) {
        const modal = new bootstrap.Modal(modalEl);
        modal.show();
    }
}

// Menerapkan Filter pada Riwayat Transaksi (Tab Transaction) di POV Penjual
function terapkanFilterRiwayat() {
    const selJenis = document.getElementById("filterJenisTrx");
    const selStatus = document.getElementById("filterStatusTrx");
    const inputKeyword = document.getElementById("filterKeywordTrx");

    const jenisDipilih = selJenis ? selJenis.value : "SEMUA";
    const statusDipilih = selStatus ? selStatus.value : "SEMUA";
    const keyword = inputKeyword ? inputKeyword.value.toLowerCase().trim() : "";

    const barisList = document.querySelectorAll(".item-riwayat-row");
    let visibleCount = 0;

    barisList.forEach(row => {
        const rowJenis = row.getAttribute("data-jenis") || "";
        const rowStatus = row.getAttribute("data-status") || "";
        const rowResi = (row.getAttribute("data-resi") || "").toLowerCase();
        const rowItem = (row.getAttribute("data-item") || "").toLowerCase();
        const rowPenjual = (row.getAttribute("data-penjual") || "").toLowerCase();
        const rowPetugas = (row.getAttribute("data-petugas") || "").toLowerCase();
        const rowAlamat = (row.getAttribute("data-alamat") || "").toLowerCase();

        const cocokJenis = (jenisDipilih === "SEMUA" || rowJenis === jenisDipilih);
        const cocokStatus = (statusDipilih === "SEMUA" || rowStatus.toLowerCase() === statusDipilih.toLowerCase());
        const cocokKeyword = (keyword === "" ||
            rowResi.includes(keyword) ||
            rowItem.includes(keyword) ||
            rowPenjual.includes(keyword) ||
            rowPetugas.includes(keyword) ||
            rowAlamat.includes(keyword));

        if (cocokJenis && cocokStatus && cocokKeyword) {
            row.classList.remove("d-none");
            visibleCount++;
        } else {
            row.classList.add("d-none");
        }
    });

    const pesanKosong = document.getElementById("pesanKosongRiwayat");
    if (pesanKosong) {
        if (visibleCount === 0) {
            pesanKosong.classList.remove("d-none");
        } else {
            pesanKosong.classList.add("d-none");
        }
    }
}

// Direct URL saat klik tombol '+' di katalog produk: tidak memunculkan popup alamat, langsung direct ke check-it-now redeem
function konfirmasiTukarPoin(idProduk, namaProduk, hargaPoin, stokBarang) {
    if (stokBarang !== undefined && stokBarang <= 0) {
        Swal.fire({
            icon: 'error',
            title: 'Stok Habis',
            text: 'Maaf, stok produk olahan ini sedang kosong.',
            confirmButtonColor: '#F7B32E'
        });
        return;
    }
    window.location.href = '/check-it-now?tab=redeem&produkId=' + idProduk;
}


// Mengisi form Kelola Produk Olahan di POV Pusat saat tombol Edit Produk diklik
function isiFormEditProduk(btnEl) {
    if (!btnEl) return;
    document.getElementById("formProdukId").value = btnEl.getAttribute("data-id") || "";
    document.getElementById("formProdukNama").value = btnEl.getAttribute("data-nama") || "";
    document.getElementById("formProdukPoin").value = btnEl.getAttribute("data-poin") || "";
    document.getElementById("formProdukStok").value = btnEl.getAttribute("data-stok") || "";
    document.getElementById("formProdukDeskripsi").value = btnEl.getAttribute("data-deskripsi") || "";
    const fileInput = document.getElementById("formProdukGambar");
    if (fileInput) fileInput.required = false;

    const titleEl = document.getElementById("judulFormProduk");
    if (titleEl) titleEl.innerText = "Edit Produk Olahan: " + (btnEl.getAttribute("data-nama") || "");
    const btnSubmit = document.getElementById("btnSubmitProduk");
    if (btnSubmit) btnSubmit.innerText = "Update Produk";
    const btnBatal = document.getElementById("btnBatalEditProduk");
    if (btnBatal) btnBatal.classList.remove("d-none");

    document.getElementById("cardFormProduk")?.scrollIntoView({ behavior: "smooth", block: "start" });
}

// Mereset form Kelola Produk kembali ke mode Tambah Produk Baru
function resetFormProduk() {
    document.getElementById("formProdukId").value = "";
    document.getElementById("formProdukNama").value = "";
    document.getElementById("formProdukPoin").value = "";
    document.getElementById("formProdukStok").value = "";
    document.getElementById("formProdukDeskripsi").value = "";
    const fileInput = document.getElementById("formProdukGambar");
    if (fileInput) {
        fileInput.value = "";
        fileInput.required = false;
    }
    const titleEl = document.getElementById("judulFormProduk");
    if (titleEl) titleEl.innerText = "Tambah Produk Olahan Baru (UC-03)";
    const btnSubmit = document.getElementById("btnSubmitProduk");
    if (btnSubmit) btnSubmit.innerText = "Simpan Produk";
    const btnBatal = document.getElementById("btnBatalEditProduk");
    if (btnBatal) btnBatal.classList.add("d-none");
}

// Mengisi form Kelola Petugas & Hak Akses di POV Pusat saat tombol Edit Petugas diklik
function isiFormEditPetugas(btnEl) {
    if (!btnEl) return;
    document.getElementById("formPetugasId").value = btnEl.getAttribute("data-id") || "";
    document.getElementById("formPetugasNama").value = btnEl.getAttribute("data-nama") || "";
    document.getElementById("formPetugasEmail").value = btnEl.getAttribute("data-email") || "";
    document.getElementById("formPetugasTelp").value = btnEl.getAttribute("data-telp") || "";
    document.getElementById("formPetugasKapasitas").value = btnEl.getAttribute("data-kapasitas") || "200";
    document.getElementById("formPetugasAlamat").value = btnEl.getAttribute("data-alamat") || "";
    document.getElementById("formPetugasRole").value = btnEl.getAttribute("data-role") || "PETUGAS";
    document.getElementById("formPetugasStatus").value = (btnEl.getAttribute("data-status") || "tersedia").toLowerCase();

    const passInput = document.getElementById("formPetugasPassword");
    if (passInput) {
        passInput.value = "";
        passInput.required = false;
        passInput.placeholder = "Kosongkan jika password tidak diubah";
    }

    const titleEl = document.getElementById("judulFormPetugas");
    if (titleEl) titleEl.innerText = "Edit Data & Hak Akses Petugas: " + (btnEl.getAttribute("data-nama") || "");
    const btnSubmit = document.getElementById("btnSubmitPetugas");
    if (btnSubmit) btnSubmit.innerText = "Update Data & Hak Akses";
    const btnBatal = document.getElementById("btnBatalEditPetugas");
    if (btnBatal) btnBatal.classList.remove("d-none");

    document.getElementById("cardFormPetugas")?.scrollIntoView({ behavior: "smooth", block: "start" });
}

// Mereset form Kelola Petugas kembali ke mode Pendaftaran Petugas Baru
function resetFormPetugas() {
    document.getElementById("formPetugasId").value = "";
    document.getElementById("formPetugasNama").value = "";
    document.getElementById("formPetugasEmail").value = "";
    document.getElementById("formPetugasTelp").value = "";
    document.getElementById("formPetugasKapasitas").value = "200";
    document.getElementById("formPetugasAlamat").value = "";
    document.getElementById("formPetugasRole").value = "PETUGAS";
    document.getElementById("formPetugasStatus").value = "tersedia";

    const passInput = document.getElementById("formPetugasPassword");
    if (passInput) {
        passInput.value = "";
        passInput.required = true;
        passInput.placeholder = "Password login petugas";
    }

    const titleEl = document.getElementById("judulFormPetugas");
    if (titleEl) titleEl.innerText = "Pendaftaran Petugas & Pemberian Hak Akses Role";
    const btnSubmit = document.getElementById("btnSubmitPetugas");
    if (btnSubmit) btnSubmit.innerText = "Daftarkan & Beri Hak Akses";
    const btnBatal = document.getElementById("btnBatalEditPetugas");
    if (btnBatal) btnBatal.classList.add("d-none");
}


// Menyimpan state kategori katalog yang sedang aktif agar sinkron dengan search bar
let activeCatalogCategory = "All";

// Memfilter kartu katalog produk berdasarkan pencarian dan pill kategori (FR-02)
function filterKatalogProduk(kategoriPilihan, btnElement) {
    if (kategoriPilihan) {
        activeCatalogCategory = kategoriPilihan;
    }
    if (btnElement) {
        document.querySelectorAll(".category-pill-btn").forEach(b => {
            b.classList.remove("btn-jlt-primary");
            b.classList.add("btn-jlt-outline");
        });
        btnElement.classList.remove("btn-jlt-outline");
        btnElement.classList.add("btn-jlt-primary");
    }
    const keyword = (document.getElementById("searchKatalogInput")?.value || "").toLowerCase().trim();
    const synonymMap = {
        "All": [],
        "Aroma": ["aroma", "diffuser", "essential", "terapi", "therapy", "wangi", "pengharum"],
        "Lilin": ["lilin", "candle", "candles", "soya", "eco"],
        "Mainan": ["mainan", "toy", "toys", "puzzle", "edukasi", "blok"],
        "Sabun": ["sabun", "soap", "cuci", "clean", "gel", "piring", "tangan"]
    };

    let visibleCount = 0;
    document.querySelectorAll(".product-item-col").forEach(col => {
        const nama = (col.getAttribute("data-nama") || "").toLowerCase();
        const desk = (col.getAttribute("data-deskripsi") || "").toLowerCase();
        const kat = (col.getAttribute("data-kategori") || "").toLowerCase();
        const gabunganTeks = nama + " " + desk + " " + kat;

        const cocokKata = (keyword === "" || gabunganTeks.includes(keyword));
        let cocokKat = true;
        if (activeCatalogCategory && activeCatalogCategory !== "All") {
            const keywordsKat = synonymMap[activeCatalogCategory] || [activeCatalogCategory.toLowerCase()];
            cocokKat = keywordsKat.some(k => gabunganTeks.includes(k));
        }

        if (cocokKata && cocokKat) {
            col.classList.remove("d-none");
            col.style.display = "";
            visibleCount++;
        } else {
            col.classList.add("d-none");
            col.style.display = "none";
        }
    });

    const emptyCatalogMsg = document.getElementById("emptyCatalogFilterMsg");
    if (emptyCatalogMsg) {
        emptyCatalogMsg.classList.toggle("d-none", visibleCount > 0);
    }
}

// Mengaktifkan Browser Push Notification (Web Notification API) + SweetAlert Toast
function aktifkanPushNotification() {
    if (!("Notification" in window)) {
        Swal.fire({ icon: 'info', title: 'Info Browser', text: 'Browser Anda menggunakan pusat notifikasi dalam aplikasi.', confirmButtonColor: '#FFCA00' });
        return;
    }
    Notification.requestPermission().then(permission => {
        if (permission === "granted") {
            new Notification("Jelantara Push Notification Aktif!", {
                body: "Anda akan menerima notifikasi update transaksi, penugasan kurir, dan redeem poin secara langsung."
            });
            Swal.fire({ icon: 'success', title: 'Push Notification Aktif!', text: 'Notifikasi browser telah diaktifkan untuk akun Anda.', confirmButtonColor: '#FFCA00' });
        }
    });
}


// Mengatur pilihan dropdown Nama Penyedia secara dinamis berdasarkan Metode Pembayaran (Bank vs E-Wallet)
function updatePilihanPenyedia(selectMetodeId, selectPenyediaId, labelNomorId, inputNomorId, selectedDefault) {
    const selMetode = document.getElementById(selectMetodeId);
    const selPenyedia = document.getElementById(selectPenyediaId);
    if (!selMetode || !selPenyedia) return;

    const metode = selMetode.value;
    const opsiBank = ["BCA", "Mandiri", "BRI", "BNI"];
    const opsiEwallet = ["GoPay", "DANA"];
    const daftarOpsi = (metode === "E-Wallet") ? opsiEwallet : opsiBank;

    const nilaiSebelumnya = selectedDefault || selPenyedia.getAttribute("data-selected") || selPenyedia.value;
    selPenyedia.innerHTML = "";
    daftarOpsi.forEach(item => {
        const opt = document.createElement("option");
        opt.value = item;
        opt.textContent = (metode === "E-Wallet") ? (item + " (E-Wallet)") : ("Bank " + item);
        if (nilaiSebelumnya && nilaiSebelumnya.toUpperCase() === item.toUpperCase()) {
            opt.selected = true;
        }
        selPenyedia.appendChild(opt);
    });

    const lblNomor = document.getElementById(labelNomorId);
    const inpNomor = document.getElementById(inputNomorId);
    if (lblNomor) {
        lblNomor.innerHTML = (metode === "E-Wallet")
            ? 'Nomor E-Wallet (GoPay / DANA) <span class="text-danger">*</span>'
            : 'Nomor Rekening Bank <span class="text-danger">*</span>';
    }
    if (inpNomor) {
        inpNomor.placeholder = (metode === "E-Wallet")
            ? "Contoh: 081234567890"
            : "Contoh: 7210998212";
    }
}

// Mengatur tampilan alamat saat ini vs input alamat kustom pada form Order Pickup & Redeem Poin
function togglePilihanAlamat(radioName, boxSaatIniId, boxKustomId, inputKustomId) {
    const selected = document.querySelector('input[name="' + radioName + '"]:checked');
    const isKustom = selected && selected.value === "KUSTOM";
    const boxSaatIni = document.getElementById(boxSaatIniId);
    const boxKustom = document.getElementById(boxKustomId);
    const inpKustom = document.getElementById(inputKustomId);

    if (boxSaatIni) boxSaatIni.classList.toggle("d-none", isKustom);
    if (boxKustom) boxKustom.classList.toggle("d-none", !isKustom);
    if (inpKustom) inpKustom.required = isKustom;
}

// Engine Pagination Bootstrap Otomatis agar daftar kartu tidak memanjang ke bawah
function initBootstrapPagination(containerId, itemSelector, itemsPerPage) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const items = Array.from(container.querySelectorAll(itemSelector));
    const navWrap = document.getElementById(containerId + "-pagination");
    if (items.length <= itemsPerPage) {
        if (navWrap) navWrap.innerHTML = "";
        return;
    }

    let currentPage = 1;
    const totalPages = Math.ceil(items.length / itemsPerPage);

    function renderPage(page) {
        currentPage = Math.max(1, Math.min(page, totalPages));
        const start = (currentPage - 1) * itemsPerPage;
        const end = start + itemsPerPage;

        items.forEach((el, idx) => {
            if (idx >= start && idx < end) {
                el.classList.remove("d-none");
            } else {
                el.classList.add("d-none");
            }
        });

        if (!navWrap) return;
        let html = '<nav aria-label="Page navigation"><ul class="pagination jlt-pagination justify-content-center align-items-center flex-wrap mb-0 mt-3">';
        html += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                    <button type="button" class="page-link" data-page="${currentPage - 1}">&laquo; Prev</button>
                 </li>`;
        for (let i = 1; i <= totalPages; i++) {
            html += `<li class="page-item ${i === currentPage ? 'active' : ''}">
                        <button type="button" class="page-link" data-page="${i}">${i}</button>
                     </li>`;
        }
        html += `<li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                    <button type="button" class="page-link" data-page="${currentPage + 1}">Next &raquo;</button>
                 </li>`;
        html += '</ul></nav>';
        navWrap.innerHTML = html;

        navWrap.querySelectorAll("button.page-link").forEach(btn => {
            btn.addEventListener("click", function () {
                const targetPage = parseInt(this.getAttribute("data-page"), 10);
                if (!isNaN(targetPage)) renderPage(targetPage);
            });
        });
    }

    renderPage(1);
}

// Inisialisasi semua dropdown pembayaran & pagination saat halaman selesai dimuat
document.addEventListener("DOMContentLoaded", function () {
    updatePilihanPenyedia("metodePembayaranOnboarding", "namaPenyediaOnboarding", "labelNomorOnboarding", "inputNomorOnboarding");
    updatePilihanPenyedia("metodePembayaranOrder", "namaPenyediaOrder", "labelNomorOrder", "inputNomorOrder");
    updatePilihanPenyedia("metodePembayaranProfile", "namaPenyediaProfile", "labelNomorProfile", "inputNomorProfile");

    initBootstrapPagination("listPetugasContainer", ".petugas-page-item", 4);
    initBootstrapPagination("listTransaksiPusatContainer", ".trx-pusat-page-item", 4);
    initBootstrapPagination("listProdukPusatContainer", ".produk-pusat-page-item", 6);
    initBootstrapPagination("listRedeemPusatContainer", ".redeem-pusat-page-item", 4);
    initBootstrapPagination("listRiwayatPenjualContainer", ".item-riwayat-row", 5);
    initBootstrapPagination("listTugasPetugasContainer", ".tugas-ptg-page-item", 4);
    initBootstrapPagination("listRedeemPetugasContainer", ".redeem-ptg-page-item", 4);
});
