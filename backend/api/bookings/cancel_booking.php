<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['bookingId'])) {
    echo json_encode(["success" => false, "message" => "Missing bookingId"]);
    exit;
}

$bookingId = (int)$data['bookingId'];

try {
    $pdo->beginTransaction();

    $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ?");
    $stmt->execute([$bookingId]);
    $booking = $stmt->fetch();

    if (!$booking) {
        $pdo->rollBack();
        echo json_encode(["success" => false, "message" => "Booking not found"]);
        exit;
    }

    $slotId = $booking['slot_id'];

    // Mark booking as Cancelled, queuePosition = 0
    $stmt = $pdo->prepare("UPDATE bookings SET status = 'Cancelled', queue_position = 0 WHERE booking_id = ?");
    $stmt->execute([$bookingId]);

    // Decrement slot current tokens
    $stmt = $pdo->prepare("UPDATE slots SET currentTokens = GREATEST(0, currentTokens - 1) WHERE id = ?");
    $stmt->execute([$slotId]);

    // Recalculate remaining queues
    $stmt = $pdo->prepare("SELECT booking_id FROM bookings WHERE slot_id = ? AND status IN ('Waiting', 'In Premise', 'Called') ORDER BY booking_time ASC");
    $stmt->execute([$slotId]);
    $remaining = $stmt->fetchAll();

    $pos = 1;
    foreach ($remaining as $r) {
        $stmt = $pdo->prepare("UPDATE bookings SET queue_position = ? WHERE booking_id = ?");
        $stmt->execute([$pos++, $r['booking_id']]);
    }

    $pdo->commit();
    echo json_encode(["success" => true, "message" => "Cancelled successfully"]);
} catch (Exception $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
