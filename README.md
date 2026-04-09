cd /Applications/development/Java/Autoklik/autostate
printf "protocol=https\nhost=github.com\n" | git credential-osxkeychain erase
git push -u origin maincd /Applications/development/Java/Autoklik/autostate
printf "protocol=https\nhost=github.com\n" | git credential-osxkeychain erase
git push -u origin main# Bota Auto Klik

Aplikasi auto-clicker berbasis Java dengan GUI yang menggunakan Selenium WebDriver untuk mengotomatisasi interaksi dengan website.

## Fitur

- 🖱️ **Multiple XPath Support** - Mendukung 3 XPath untuk berbagai aksi (klik awal, set quantity, buy)
- ⏰ **Time-based Scheduling** - Jalankan aksi otomatis pada waktu tertentu
- 📝 **History Persistence** - URL dan XPath disimpan untuk penggunaan berikutnya
- 📋 **Real-time Logging** - Log aktivitas dalam GUI
- 🌐 **Chrome Browser Control** - Otomatis membuka dan mengontrol Chrome
- ⌨️ **Copy-Paste Support** - Mudah menyalin teks dari sumber lain

## Persyaratan

- Java 11 atau lebih tinggi
- Maven
- Chrome/Chromium Browser
- ChromeDriver (sesuai dengan versi Chrome)

## Instalasi

1. Clone repository:
```bash
git clone https://github.com/fadhlysyahputra02/Bota-Auto-Klik.git
cd Bota-Auto-Klik/autostate
```

2. Compile project dengan Maven:
```bash
mvn clean compile
```

3. Jalankan aplikasi:
```bash
mvn exec:java -Dexec.mainClass="com.autoklik.App"
```

## Penggunaan

1. Masukkan URL website target di field **URL**
2. Masukkan XPath untuk klik awal di field **FULL XPATH**
3. (Opsional) Masukkan XPath untuk set quantity di **XPATH ke-2 (Qty)**
4. (Opsional) Masukkan XPath untuk tombol beli di **XPATH ke-3 (Buy)**
5. Masukkan jumlah item (default: 1)
6. Set waktu eksekusi dalam format HH:mm:ss
7. Klik tombol **START** untuk memulai proses

## Technology Stack

- **Java 11** - Bahasa pemrograman utama
- **Selenium WebDriver 4.17.0** - Browser automation
- **Swing** - GUI Framework (Nimbus Look and Feel)
- **Maven** - Build tool
- **Timezone**: Asia/Jakarta

## Struktur Project

```
autostate/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/autoklik/App.java
│   │   └── resources/
│   └── test/
└── .gitignore
```

## Catatan Penting

- Aplikasi membuka Chrome secara otomatis
- Chrome akan tetap terbuka setelah eksekusi untuk inspeksi manual
- Gunakan DevTools di Chrome untuk mengidentifikasi XPath yang tepat
- Semua history disimpan di file `xpath_history.txt`

## License

Proprietary - Penggunaan pribadi

## Author

Fadhly Syahputra
