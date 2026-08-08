<?php
require_once '../../config/database.php';

$data = json_decode(file_get_contents("php://input"), true);
$id = isset($data['id']) ? (int)$data['id'] : 0;

if ($id <= 0) {
    echo json_encode(["success" => false, "message" => "Invalid user ID"]);
    exit;
}

try {
    $stmt1 = $pdo->prepare("DELETE FROM users WHERE id = ?");
    $stmt1->execute([$id]);
    
    $stmt2 = $pdo->prepare("DELETE FROM providers WHERE provider_id = ? OR id = ?");
    $stmt2->execute([$id, $id]);

    $stmt3 = $pdo->prepare("DELETE FROM service_centers WHERE providerId = ? OR adminId = ?");
    $stmt3->execute([$id, $id]);
    
    echo json_encode(["success" => true, "message" => "User/Provider and associated centers deleted successfully"]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
