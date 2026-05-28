@echo off
title Menjalankan DoBarPOS...
set "BASE_DIR=%~dp0"
set "CLASSPATH=%BASE_DIR%build\classes;%BASE_DIR%lib\mysql-connector-j.jar;%BASE_DIR%lib\itextpdf-5.5.13.3.jar"

echo Menjalankan aplikasi DoBarPOS...
java -cp "%CLASSPATH%" dobarpos.LoginFrame
if %errorlevel% neq 0 (
    echo.
    echo Terjadi kesalahan saat menjalankan aplikasi.
    echo Pastikan Java sudah terinstall dan MySQL (XAMPP) sudah aktif.
    pause
)
