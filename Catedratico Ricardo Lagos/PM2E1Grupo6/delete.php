<?php
ini_set('display_errors', 0);
error_reporting(0);
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Método no permitido, use POST']);
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
    $input = json_decode(file_get_contents('php://input'), true);
    if (!$input || !isset($input['id'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Se requiere el campo id']);
        exit;
    }
    $id = (int)$input['id'];

    $stmt = $pdo->prepare('DELETE FROM audios WHERE id = :id');
    $stmt->bindValue(':id', $id, PDO::PARAM_INT);
    $stmt->execute();

    $rows = $stmt->rowCount();
    echo json_encode([
        'success'       => true,
        'message'       => $rows > 0 ? 'Registro eliminado correctamente.' : 'No se encontró el registro con ese ID.',
        'affected_rows' => $rows
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error en la base de datos: ' . $e->getMessage()]);
}
