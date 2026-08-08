<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['id']) && !isset($data['slotId'])) {
    echo json_encode(["success" => false, "message" => "Missing slot ID"]);
    exit;
}

$id = isset($data['id']) ? (int)$data['id'] : (int)$data['slotId'];

try {
    $stmt = $pdo->prepare("DELETE FROM slots WHERE id = ?");
    $stmt->execute([$id]);
    
    echo json_encode(["success" => true, "message" => "Work slot deleted successfully"]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
