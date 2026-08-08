<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['userId'])) {
    echo json_encode(["success" => false, "message" => "Missing userId"]);
    exit;
}

$userId = (int)$data['userId'];

try {
    $stmt = $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE user_id = ?");
    $stmt->execute([$userId]);
    echo json_encode(["success" => true, "message" => "Notifications marked as read"]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
