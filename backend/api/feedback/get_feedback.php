<?php
require_once '../../config/database.php';
$centerId = isset($_GET['centerId']) ? (int)$_GET['centerId'] : null;
try {
    if ($centerId !== null) {
        $stmt = $pdo->prepare("
            SELECT f.*, u.fullname as user_name 
            FROM feedback f 
            LEFT JOIN users u ON f.user_id = u.id 
            WHERE f.center_id = ? 
            ORDER BY f.timestamp DESC
        ");
        $stmt->execute([$centerId]);
    } else {
        $stmt = $pdo->query("
            SELECT f.*, u.fullname as user_name 
            FROM feedback f 
            LEFT JOIN users u ON f.user_id = u.id 
            ORDER BY f.timestamp DESC
        ");
    }
    $feedbacks = $stmt->fetchAll();
    
    $formatted = [];
    foreach ($feedbacks as $f) {
        $formatted[] = [
            "id" => (int)$f['feedback_id'],
            "userId" => (int)$f['user_id'],
            "userName" => $f['user_name'] ?? 'Anonymous',
            "centerId" => $f['center_id'] !== null ? (int)$f['center_id'] : null,
            "rating" => (float)$f['rating'],
            "comments" => $f['comments'],
            "timestamp" => (float)$f['timestamp']
        ];
    }
    echo json_encode(["success" => true, "feedbacks" => $formatted]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
