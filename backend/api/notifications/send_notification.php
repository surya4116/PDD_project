<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['userId']) || !isset($data['title']) || !isset($data['message'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$userId = (int)$data['userId'];
$title = $data['title'];
$message = $data['message'];

try {
    $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)");
    $stmt->execute([$userId, $title, $message]);
    $notificationId = $pdo->lastInsertId();
    
    echo json_encode(["success" => true, "message" => "Notification dispatched", "notificationId" => (int)$notificationId]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
