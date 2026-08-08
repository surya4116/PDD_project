<?php
require_once '../../config/database.php';
$slotId = isset($_GET['slotId']) ? (int)$_GET['slotId'] : null;

if ($slotId === null) {
    echo json_encode(["success" => false, "message" => "Missing slot ID"]);
    exit;
}

try {
    $stmt = $pdo->prepare("
        SELECT * FROM bookings 
        WHERE slot_id = ? AND status != 'Completed' AND status != 'Failed' AND status != 'Cancelled'
        ORDER BY queue_position ASC
    ");
    $stmt->execute([$slotId]);
    $queue = $stmt->fetchAll();
    
    $formatted = [];
    foreach ($queue as $q) {
        $formatted[] = [
            "id" => (int)$q['booking_id'],
            "userId" => (int)$q['user_id'],
            "slotId" => (int)$q['slot_id'],
            "centerId" => (int)$q['center_id'],
            "tokenNumber" => $q['token_number'],
            "queuePosition" => (int)$q['queue_position'],
            "status" => $q['status'],
            "bookingTime" => (float)$q['booking_time']
        ];
    }
    
    echo json_encode(["success" => true, "queue" => $formatted]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
