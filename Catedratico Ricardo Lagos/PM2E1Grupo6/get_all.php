<?php
ini_set('display_errors', 0);
error_reporting(0);
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Método no permitido, use GET']);
    exit;
}

require_once 'config.php';
require_once 'validar_token.php';
if (!validarApiKey()) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'API-Key inválida o no provista']);
    exit;
}



try {
    $stmt   = $pdo->query('SELECT id, descripcion, audio, imagen, lat, lng, fecha FROM audios ORDER BY id DESC');
    $audios = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(['success' => true, 'data' => $audios], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error al consultar: ' . $e->getMessage()]);
}
