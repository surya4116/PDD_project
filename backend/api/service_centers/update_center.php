<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['id']) || !isset($data['name']) || !isset($data['address']) || !isset($data['isActive'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$id = (int)$data['id'];
$name = $data['name'];
$address = $data['address'];
$status = $data['isActive'] ? 'Active' : 'Inactive';

try {
    $stmt = $pdo->prepare("UPDATE service_centers SET center_name = ?, address = ?, status = ? WHERE center_id = ?");
    $stmt->execute([$name, $address, $status, $id]);
    
    echo json_encode(["success" => true, "message" => "Service Center updated"]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
