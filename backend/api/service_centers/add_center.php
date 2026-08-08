<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

// Support both 'providerId' (new) and 'adminId' (legacy) field names
$providerIdKey = isset($data['providerId']) ? 'providerId' : (isset($data['adminId']) ? 'adminId' : null);

if (!isset($data['name']) || !isset($data['categoryId']) || !isset($data['address']) || $providerIdKey === null) {
    echo json_encode(["success" => false, "message" => "Missing fields (name, categoryId, address, providerId required)"]);
    exit;
}

$name = $data['name'];
$categoryId = (int)$data['categoryId'];
$address = $data['address'];
$providerId = (int)$data[$providerIdKey];

try {
    $stmt = $pdo->prepare("INSERT INTO service_centers (center_name, categoryId, address, providerId) VALUES (?, ?, ?, ?)");
    $stmt->execute([$name, $categoryId, $address, $providerId]);
    $centerId = $pdo->lastInsertId();
    
    echo json_encode(["success" => true, "message" => "Service Center added", "centerId" => (int)$centerId]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
