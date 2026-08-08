<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);
if (!isset($data['slotId'])) {
    echo json_encode(["success" => false, "message" => "Missing slotId"]);
    exit;
}
$slotId = (int)$data['slotId'];
try {
    $stmt = $pdo->prepare("SELECT booking_id FROM bookings WHERE slot_id = ? AND status IN ('Waiting', 'In Premise', 'Called') ORDER BY booking_time ASC");
    $stmt->execute([$slotId]);
    $remaining = $stmt->fetchAll();

    $pos = 1;
    foreach ($remaining as $r) {
        $stmt = $pdo->prepare("UPDATE bookings SET queue_position = ? WHERE booking_id = ?");
        $stmt->execute([$pos++, $r['booking_id']]);
    }
    echo json_encode(["success" => true, "message" => "Queue updated successfully"]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
