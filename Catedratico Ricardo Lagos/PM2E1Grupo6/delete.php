<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php'; // Aquí ya tienes la conexión PDO $pdo

try {
    $input = json_decode(file_get_contents('php://input'), true);

    if (!$input || !isset($input['id'])) {
        throw new Exception('Se requiere el ID del registro a eliminar.');
    }

    $id = (int)$input['id'];

    $sql = "DELETE FROM audios WHERE id = :id";  // tabla correcta audios
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':id', $id, PDO::PARAM_INT);
    $stmt->execute();

    $rows = $stmt->rowCount();

    echo json_encode([
        'success' => true,
        'message' => $rows > 0 ? 'Registro eliminado correctamente.' : 'No se encontró el registro con ese ID.',
        'affected_rows' => $rows
    ]);

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
