<?php
require_once 'config.php';

// Verifica si el token viene en el encabezado
$headers = apache_request_headers();
if (!isset($headers['API-Key'])) {
    http_response_code(401);
    echo json_encode(["issuccess" => false, "message" => "API Key requerida"]);
    exit;
}

$apiKey = $headers['API-Key'];

$stmt = $pdo->prepare("SELECT * FROM api_tokens WHERE token = ?");
$stmt->execute([$apiKey]);

if ($stmt->rowCount() === 0) {
    http_response_code(403);
    echo json_encode(["issuccess" => false, "message" => "API Key inválida"]);
    exit;
}
?>
