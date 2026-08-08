<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);
if (!isset($data['id']) || !isset($data['name']) || !isset($data['duration'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}
try {
    $stmt = $pdo->prepare("UPDATE services SET name = ?, duration = ? WHERE service_id = ?");
    $stmt->execute([$data['name'], $data['duration'], (int)$data['id']]);
    echo json_encode(["success" => true]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
