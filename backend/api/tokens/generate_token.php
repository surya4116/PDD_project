<?php
require_once '../../config/database.php';
$centerId = isset($_GET['centerId']) ? (int)$_GET['centerId'] : 0;
$prefix = isset($_GET['prefix']) ? $_GET['prefix'] : 'TKN';
try {
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM bookings WHERE center_id = ? AND token_number LIKE ?");
    $stmt->execute([$centerId, "$prefix%"]);
    $count = $stmt->fetchColumn();
    $newToken = $prefix . str_pad($count + 1, 3, '0', STR_PAD_LEFT);
    echo json_encode(["success" => true, "token" => $newToken]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
