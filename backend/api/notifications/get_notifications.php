<?php
require_once '../../config/database.php';

$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;

if ($userId === null) {
    echo json_encode(["success" => false, "message" => "Missing user ID"]);
    exit;
}

try {
    $stmt = $pdo->prepare("SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC");
    $stmt->execute([$userId]);
    $rows = $stmt->fetchAll();
    
    $notifications = [];
    foreach ($rows as $r) {
        $notifications[] = [
            "id" => (int)$r['notification_id'],
            "userId" => (int)$r['user_id'],
            "title" => $r['title'],
            "message" => $r['message'],
            "timestamp" => strtotime($r['created_at']) * 1000,
            "isRead" => (bool)$r['is_read']
        ];
    }
    
    echo json_encode(["success" => true, "notifications" => $notifications]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
