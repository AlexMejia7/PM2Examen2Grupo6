<?php
header("Content-Type: application/json; charset=utf-8");
require_once 'config.php';
require_once 'validar_token.php'; // Validar API Key

// Obtener datos JSON del cuerpo
$data = json_decode(file_get_contents("php://input"));

if (
    isset($data->descripcion) &&
    isset($data->audio) &&
    isset($data->imagen) &&
    isset($data->lat) &&
    isset($data->lng)
) {
    try {
        $sql = "INSERT INTO audios (descripcion, audio, imagen, lat, lng) VALUES (:descripcion, :audio, :imagen, :lat, :lng)";
        $stmt = $pdo->prepare($sql);

        // Bind params
        $stmt->bindParam(':descripcion', $data->descripcion, PDO::PARAM_STR);
        $stmt->bindParam(':audio', $data->audio, PDO::PARAM_STR);
        $stmt->bindParam(':imagen', $data->imagen, PDO::PARAM_STR);
        $stmt->bindParam(':lat', $data->lat);
        $stmt->bindParam(':lng', $data->lng);

        if ($stmt->execute()) {
            echo json_encode([
                "success" => true,
                "message" => "Registro guardado exitosamente"
            ]);
        } else {
            echo json_encode([
                "success" => false,
                "message" => "No se pudo guardar el registro"
            ]);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            "success" => false,
            "message" => "Error en la base de datos: " . $e->getMessage()
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Faltan campos requeridos"
    ]);
}
