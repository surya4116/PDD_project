<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['id'])) {
    echo json_encode(["success" => false, "message" => "Missing center ID"]);
    exit;
}

$id = (int)$data['id'];

try {
    $stmt = $pdo->prepare("DELETE FROM service_centers WHERE center_id = ?");
    $stmt->execute([$id]);
    
    echo json_encode(["success" => true, "message" => "Service Center deleted"]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
