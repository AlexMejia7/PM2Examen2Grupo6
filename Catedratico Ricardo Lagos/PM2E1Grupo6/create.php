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



$input = json_decode(file_get_contents('php://input'), true);
if (
    empty($input['descripcion']) ||
    empty($input['audio'])       ||
    empty($input['imagen'])      ||
    !isset($input['lat'])        ||
    !isset($input['lng'])
) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Faltan campos requeridos']);
    exit;
}

try {
    $sql = 'INSERT INTO audios (descripcion, audio, imagen, lat, lng) VALUES (:descripcion, :audio, :imagen, :lat, :lng)';
    $stmt = $pdo->prepare($sql);
    $stmt->bindValue(':descripcion', $input['descripcion'], PDO::PARAM_STR);
    $stmt->bindValue(':audio',       $input['audio'],       PDO::PARAM_STR);
    $stmt->bindValue(':imagen',      $input['imagen'],      PDO::PARAM_STR);
    $stmt->bindValue(':lat',         $input['lat']);
    $stmt->bindValue(':lng',         $input['lng']);

    if ($stmt->execute()) {
        http_response_code(201);
        echo json_encode(['success' => true, 'message' => 'Registro guardado exitosamente', 'id' => $pdo->lastInsertId()]);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'No se pudo guardar el registro']);
    }
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error en la base de datos: ' . $e->getMessage()]);
}
