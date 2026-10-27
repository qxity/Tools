# Facebook Friend Tools

Aplikasi Android pendamping untuk mengelola daftar profil yang ingin ditinjau. Aplikasi ini tidak berafiliasi dengan Meta/Facebook.

## Fitur versi awal
- Dashboard biru Facebook dengan dark surface.
- Daftar URL profil yang ditambahkan sendiri.
- Membuka saran teman Facebook atau URL profil melalui aplikasi/browser.
- Tombol peninjauan dan penandaan status manual.
- Riwayat sementara selama aplikasi berjalan.
- Tidak meminta kredensial Facebook, tidak melakukan scraping, dan tidak mengirim permintaan pertemanan secara otomatis.

## Persyaratan
- Android Studio terbaru yang mendukung Android Gradle Plugin 8.7.x.
- JDK 17.
- Android SDK Platform 35.
- Perangkat Android 13 (API 33) atau lebih baru.

## Build lokal
1. Ekstrak ZIP dan buka folder `FacebookFriendTools` di Android Studio.
2. Pastikan JDK 17 dan Android SDK 35 terpasang.
3. Sync Gradle.
4. Jalankan aplikasi di perangkat Android 13+ atau pilih **Build > Build APK(s)**.
5. APK debug ada di `app/build/outputs/apk/debug/app-debug.apk`.

Alternatif terminal pada komputer yang telah memasang Gradle dan Android SDK:
```bash
gradle --no-daemon assembleDebug
```

## Build otomatis GitHub Actions
1. Buat repository baru di GitHub.
2. Unggah seluruh isi folder ini (folder `.github` harus ikut).
3. Buka tab **Actions**.
4. Workflow **Android APK Build** berjalan otomatis setelah push; bisa juga dijalankan lewat **Run workflow**.
5. Buka run yang selesai, lalu unduh artifact `Facebook-Friend-Tools-debug-apk`.
6. Ekstrak artifact ZIP untuk memperoleh `app-debug.apk`.

## Catatan penting
- Workflow menghasilkan APK debug untuk pengujian, bukan APK release yang ditandatangani untuk distribusi publik.
- Riwayat dan daftar pada versi awal masih berada di memori aplikasi; menutup proses dapat menghapus data. Room/DataStore dan ekspor CSV belum diimplementasikan pada starter ini.
- Tombol **Tandai terkirim** hanya mencatat status secara lokal. Tombol tersebut tidak mengirim permintaan kepada Facebook.
- Jangan menyimpan kata sandi atau cookie Facebook di aplikasi pihak ketiga. Patuhi ketentuan dan batas penggunaan Facebook.
