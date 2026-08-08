<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['userId']) || !isset($data['slotId']) || !isset($data['centerId'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$userId = (int)$data['userId'];
$slotId = (int)$data['slotId'];
$centerId = (int)$data['centerId'];

try {
    $pdo->beginTransaction();

    // 1. Fetch slot details
    $stmt = $pdo->prepare("SELECT * FROM slots WHERE id = ? FOR UPDATE");
    $stmt->execute([$slotId]);
    $slot = $stmt->fetch();
    if (!$slot) {
        $pdo->rollBack();
        echo json_encode(["success" => false, "message" => "Slot not found"]);
        exit;
    }

    if ($slot['currentTokens'] >= $slot['maxTokens']) {
        $pdo->rollBack();
        echo json_encode(["success" => false, "message" => "Slot Full"]);
        exit;
    }

    // 2. Fetch center & category prefix
    $stmt = $pdo->prepare("SELECT * FROM service_centers WHERE center_id = ?");
    $stmt->execute([$centerId]);
    $center = $stmt->fetch();
    if (!$center) {
        $pdo->rollBack();
        echo json_encode(["success" => false, "message" => "Service Center not found"]);
        exit;
    }

    $stmt = $pdo->prepare("SELECT * FROM categories WHERE id = ?");
    $stmt->execute([$center['categoryId']]);
    $category = $stmt->fetch();
    $prefix = $category ? $category['prefix'] : 'TKN';

    // 3. Generate Token Number
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM bookings WHERE center_id = ? AND token_number LIKE ?");
    $stmt->execute([$centerId, "$prefix%"]);
    $existingCount = $stmt->fetchColumn();
    $newNumber = $existingCount + 1;
    $tokenNumber = $prefix . str_pad($newNumber, 3, '0', STR_PAD_LEFT); // e.g. HSP001

    // 4. Calculate Queue Position
    $queuePosition = $slot['currentTokens'] + 1;

    // 5. Insert Booking record
    $bookingTime = round(microtime(true) * 1000);
    $stmt = $pdo->prepare("INSERT INTO bookings (user_id, slot_id, center_id, token_number, queue_position, status, booking_time) VALUES (?, ?, ?, ?, ?, 'Waiting', ?)");
    $stmt->execute([$userId, $slotId, $centerId, $tokenNumber, $queuePosition, $bookingTime]);
    $bookingId = $pdo->lastInsertId();

    // 6. Insert Token record
    $estimatedWaitMins = $queuePosition * 15;
    $stmt = $pdo->prepare("INSERT INTO tokens (booking_id, token_number, estimated_wait_time_mins, issued_at) VALUES (?, ?, ?, ?)");
    $stmt->execute([$bookingId, $tokenNumber, $estimatedWaitMins, $bookingTime]);
    $tokenId = $pdo->lastInsertId();

    // 7. Insert Queue record
    $stmt = $pdo->prepare("INSERT INTO queues (booking_id, token_number, position, status) VALUES (?, ?, ?, 'Waiting')");
    $stmt->execute([$bookingId, $tokenNumber, $queuePosition]);

    // 8. Insert Notification record
    $title = "Booking Confirmed";
    $message = "Your booking for $tokenNumber at " . $center['center_name'] . " is successfully confirmed. Queue Position: $queuePosition.";
    $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)");
    $stmt->execute([$userId, $title, $message]);

    // 9. Update Slot counts
    $stmt = $pdo->prepare("UPDATE slots SET currentTokens = currentTokens + 1 WHERE id = ?");
    $stmt->execute([$slotId]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Booking successful",
        "booking" => [
            "id" => (int)$bookingId,
            "userId" => $userId,
            "slotId" => $slotId,
            "centerId" => $centerId,
            "tokenNumber" => $tokenNumber,
            "queuePosition" => $queuePosition,
            "status" => "Waiting",
            "bookingTime" => $bookingTime
        ]
    ]);

} catch (Exception $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
