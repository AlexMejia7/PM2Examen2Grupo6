<?php
$host = "localhost";
$port = 3306;
$db = "audios_db";        // Usa $db para que coincida con el otro archivo
$user = "root";           // Cambié username a user para coherencia
$pass = "";               // Cambié password a pass

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8mb4", $user, $pass);
    // Mostrar errores si hay
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Conexión fallida: " . $e->getMessage());
}
