<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);
if (!isset($data['userId']) || !isset($data['rating']) || !isset($data['comments'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}
$centerId = isset($data['centerId']) ? (int)$data['centerId'] : null;
try {
    $stmt = $pdo->prepare("INSERT INTO feedback (user_id, center_id, rating, comments, timestamp) VALUES (?, ?, ?, ?, ?)");
    $timestamp = round(microtime(true) * 1000);
    $stmt->execute([
        (int)$data['userId'],
        $centerId,
        (float)$data['rating'],
        $data['comments'],
        $timestamp
    ]);
    echo json_encode(["success" => true, "id" => (int)$pdo->lastInsertId()]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
