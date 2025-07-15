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
        echo json_encode(['success' => false, 'message' => 'Se requiere el campo id.']);
        exit;
    }
    $id = (int)$input['id'];

    $fields = [];
    $params = [];

    if (isset($input['descripcion'])) {
        $fields[] = 'descripcion = :descripcion';
        $params[':descripcion'] = $input['descripcion'];
    }
    if (isset($input['audio'])) {
        $fields[] = 'audio = :audio';
        $params[':audio'] = $input['audio'];
    }
    if (isset($input['imagen'])) {
        $fields[] = 'imagen = :imagen';
        $params[':imagen'] = $input['imagen'];
    }
    if (isset($input['lat'])) {
        $fields[] = 'lat = :lat';
        $params[':lat'] = $input['lat'];
    }
    if (isset($input['lng'])) {
        $fields[] = 'lng = :lng';
        $params[':lng'] = $input['lng'];
    }

    if (empty($fields)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'No hay campos para actualizar.']);
        exit;
    }

    $params[':id'] = $id;
    $sql = 'UPDATE audios SET ' . implode(', ', $fields) . ' WHERE id = :id';
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);

    echo json_encode([
        'success'       => true,
        'message'       => 'Registro actualizado correctamente.',
        'affected_rows' => $stmt->rowCount()
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error en la base de datos: ' . $e->getMessage()]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error: ' . $e->getMessage()]);
}
