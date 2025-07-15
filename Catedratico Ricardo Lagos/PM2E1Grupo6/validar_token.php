<?php
require_once 'config.php';   // fuera de la función, así $pdo queda global

function validarApiKey(): bool
{
    global $pdo;             // usa el $pdo global

    $headers = function_exists('apache_request_headers')
        ? apache_request_headers()
        : $_SERVER;

    $apiKey = $headers['API-Key']
        ?? $headers['Http-Api-Key']
        ?? $headers['HTTP_API_KEY']
        ?? null;

    if (!$apiKey) {
        return false;
    }

    $stmt = $pdo->prepare('SELECT 1 FROM api_tokens WHERE token = ?');
    $stmt->execute([$apiKey]);

    return $stmt->fetchColumn() !== false;
}
