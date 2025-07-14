<?php
header("Content-Type: application/json; charset=utf-8");
require_once 'config.php';
require_once 'validar_token.php'; // Asegura que haya un token válido

try {
    $stmt = $pdo->query("SELECT id, descripcion, audio, imagen, lat, lng, fecha FROM audios");
    $audios = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if ($audios) {
        echo json_encode([
            "success" => true,
            "data" => $audios
        ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "No hay registros en la tabla audios."
        ]);
    }
} catch (PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Error al consultar: " . $e->getMessage()
    ]);
}
