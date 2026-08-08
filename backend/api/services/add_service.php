<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);
if (!isset($data['centerId']) || !isset($data['name']) || !isset($data['duration'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}
try {
    $stmt = $pdo->prepare("INSERT INTO services (center_id, name, duration) VALUES (?, ?, ?)");
    $stmt->execute([$data['centerId'], $data['name'], $data['duration']]);
    echo json_encode(["success" => true, "id" => (int)$pdo->lastInsertId()]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
