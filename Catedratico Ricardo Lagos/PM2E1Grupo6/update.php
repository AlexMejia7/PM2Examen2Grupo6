<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';  // incluye la conexión ya creada

try {
    // Aquí NO crees una nueva conexión PDO, usa la $pdo que viene de config.php
    // Así que eliminamos esta línea que crea otro PDO:
    // $pdo = new PDO(...);

    $input = json_decode(file_get_contents('php://input'), true);

    if (!$input || !isset($input['id'])) {
        throw new Exception('Se requiere el ID del registro a actualizar.');
    }

    $id = $input['id'];

    // Campos que quieres actualizar (ajusta según tu tabla)
    $descripcion = $input['descripcion'] ?? null;
    $audio       = $input['audio'] ?? null;
    $imagen      = $input['imagen'] ?? null;
    $lat         = $input['lat'] ?? null;
    $lng         = $input['lng'] ?? null;

    // Construir la consulta dinámicamente solo con los campos que vienen
    $fields = [];
    $params = [];

    if ($descripcion !== null) {
        $fields[] = "descripcion = :descripcion";
        $params[':descripcion'] = $descripcion;
    }
    if ($audio !== null) {
        $fields[] = "audio = :audio";
        $params[':audio'] = $audio;
    }
    if ($imagen !== null) {
        $fields[] = "imagen = :imagen";
        $params[':imagen'] = $imagen;
    }
    if ($lat !== null) {
        $fields[] = "lat = :lat";        // Ajusté nombre campo lat
        $params[':lat'] = $lat;
    }
    if ($lng !== null) {
        $fields[] = "lng = :lng";        // Ajusté nombre campo lng
        $params[':lng'] = $lng;
    }

    if (empty($fields)) {
        throw new Exception('No hay campos para actualizar.');
    }

    $params[':id'] = $id;

    $sql = "UPDATE audios SET " . implode(", ", $fields) . " WHERE id = :id"; // Usar tabla audios
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);

    echo json_encode([
        'success' => true,
        'message' => 'Registro actualizado correctamente.',
        'affected_rows' => $stmt->rowCount()
    ]);

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
