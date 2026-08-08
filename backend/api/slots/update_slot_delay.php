<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

$slotId = isset($data['slotId']) ? (int)$data['slotId'] : (isset($data['id']) ? (int)$data['id'] : null);
$delayMins = isset($data['delayMins']) ? (int)$data['delayMins'] : null;

if (!$slotId || $delayMins === null) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

try {
    $stmt = $pdo->prepare("UPDATE slots SET delayMins = delayMins + ?, status = 'Running' WHERE id = ?");
    $stmt->execute([$delayMins, $slotId]);
    echo json_encode(["success" => true]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
