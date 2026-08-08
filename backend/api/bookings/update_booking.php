<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['action'])) {
    echo json_encode(["success" => false, "message" => "Missing action"]);
    exit;
}

$action = $data['action'];

try {
    $pdo->beginTransaction();

    if ($action === 'checkin') {
        $bookingId = (int)$data['bookingId'];
        // Update booking status
        $stmt = $pdo->prepare("UPDATE bookings SET status = 'In Premise' WHERE booking_id = ?");
        $stmt->execute([$bookingId]);

        // Get center details for notification
        $stmt = $pdo->prepare("SELECT b.user_id, b.token_number, c.center_name FROM bookings b JOIN service_centers c ON b.center_id = c.center_id WHERE b.booking_id = ?");
        $stmt->execute([$bookingId]);
        $details = $stmt->fetch();
        
        if ($details) {
            $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, 'Check-In Success', ?)");
            $msg = "You have successfully checked in at " . $details['center_name'] . ". Your queue token is " . $details['token_number'] . ".";
            $stmt->execute([$details['user_id'], $msg]);
        }

        $pdo->commit();
        echo json_encode(["success" => true, "message" => "Checked in successfully"]);
        exit;
    }

    if ($action === 'reschedule') {
        $bookingId = (int)$data['bookingId'];
        $newSlotId = (int)$data['newSlotId'];

        // Get booking and old slot
        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ?");
        $stmt->execute([$bookingId]);
        $booking = $stmt->fetch();

        if (!$booking) {
            $pdo->rollBack();
            echo json_encode(["success" => false, "message" => "Booking not found"]);
            exit;
        }

        // Get new slot tokens
        $stmt = $pdo->prepare("SELECT * FROM slots WHERE id = ?");
        $stmt->execute([$newSlotId]);
        $newSlot = $stmt->fetch();

        if (!$newSlot) {
            $pdo->rollBack();
            echo json_encode(["success" => false, "message" => "New slot not found"]);
            exit;
        }

        if ($newSlot['currentTokens'] >= $newSlot['maxTokens']) {
            $pdo->rollBack();
            echo json_encode(["success" => false, "message" => "New slot is full"]);
            exit;
        }

        $newPosition = $newSlot['currentTokens'] + 1;

        // Update booking
        $stmt = $pdo->prepare("UPDATE bookings SET slot_id = ?, queue_position = ?, status = 'Waiting' WHERE booking_id = ?");
        $stmt->execute([$newSlotId, $newPosition, $bookingId]);

        // Increment new slot token count
        $stmt = $pdo->prepare("UPDATE slots SET currentTokens = currentTokens + 1 WHERE id = ?");
        $stmt->execute([$newSlotId]);

        $pdo->commit();
        echo json_encode(["success" => true, "message" => "Rescheduled successfully"]);
        exit;
    }

    if ($action === 'cancel') {
        $bookingId = (int)$data['bookingId'];

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
        exit;
    }

    if ($action === 'reject') {
        $bookingId = (int)$data['bookingId'];

        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ?");
        $stmt->execute([$bookingId]);
        $booking = $stmt->fetch();

        if (!$booking) {
            $pdo->rollBack();
            echo json_encode(["success" => false, "message" => "Booking not found"]);
            exit;
        }

        $slotId = $booking['slot_id'];

        // Mark booking as Failed, queuePosition = 0
        $stmt = $pdo->prepare("UPDATE bookings SET status = 'Failed', queue_position = 0 WHERE booking_id = ?");
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
        echo json_encode(["success" => true, "message" => "Rejected successfully"]);
        exit;
    }

    if ($action === 'complete') {
        $bookingId = (int)$data['bookingId'];

        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ?");
        $stmt->execute([$bookingId]);
        $booking = $stmt->fetch();

        if (!$booking) {
            $pdo->rollBack();
            echo json_encode(["success" => false, "message" => "Booking not found"]);
            exit;
        }

        $slotId = $booking['slot_id'];

        // Mark as completed, position 0
        $stmt = $pdo->prepare("UPDATE bookings SET status = 'Completed', queue_position = 0 WHERE booking_id = ?");
        $stmt->execute([$bookingId]);

        // Add notification
        $stmt = $pdo->prepare("SELECT c.center_name FROM service_centers c WHERE c.center_id = ?");
        $stmt->execute([$booking['center_id']]);
        $cName = $stmt->fetchColumn() ?: "Service Center";

        $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, 'Service Completed', ?)");
        $msg = "Thank you for using SmartQueue Pro! Your service for token " . $booking['token_number'] . " at " . $cName . " has been completed successfully.";
        $stmt->execute([$booking['user_id'], $msg]);

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
        echo json_encode(["success" => true, "message" => "Completed successfully"]);
        exit;
    }

    if ($action === 'call_next') {
        $slotId = (int)$data['slotId'];

        // Get remaining in slot
        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE slot_id = ? AND status IN ('Waiting', 'In Premise') ORDER BY booking_time ASC");
        $stmt->execute([$slotId]);
        $pending = $stmt->fetchAll();

        $firstPending = isset($pending[0]) ? $pending[0] : null;

        if ($firstPending) {
            $bookingId = $firstPending['booking_id'];

            // Update status to Called
            $stmt = $pdo->prepare("UPDATE bookings SET status = 'Called' WHERE booking_id = ?");
            $stmt->execute([$bookingId]);

            // Notifications
            $stmt = $pdo->prepare("SELECT center_name FROM service_centers WHERE center_id = ?");
            $stmt->execute([$firstPending['center_id']]);
            $cName = $stmt->fetchColumn() ?: "Service Center";

            // UserCalled notification
            $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, 'Counter Ready', ?)");
            $msg = "Your token " . $firstPending['token_number'] . " has been called! Please proceed to the service counter at " . $cName . ".";
            $stmt->execute([$firstPending['user_id'], $msg]);

            // Alert the next in line
            $nextPending = isset($pending[1]) ? $pending[1] : null;
            if ($nextPending) {
                $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, 'Your Turn Next', ?)");
                $msg2 = "Your token " . $nextPending['token_number'] . " is next in line at " . $cName . ". Please get ready.";
                $stmt->execute([$nextPending['user_id'], $msg2]);
            }

            $pdo->commit();
            echo json_encode(["success" => true, "message" => "Called successfully"]);
        } else {
            $pdo->rollBack();
            echo json_encode(["success" => false, "message" => "No waiting bookings found"]);
        }
        exit;
    }

    if ($action === 'skip') {
        $bookingId = (int)$data['bookingId'];

        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ?");
        $stmt->execute([$bookingId]);
        $booking = $stmt->fetch();

        if (!$booking) {
            $pdo->rollBack();
            echo json_encode(["success" => false, "message" => "Booking not found"]);
            exit;
        }

        $slotId = $booking['slot_id'];

        // Mark booking as Cancelled (Skipped), queuePosition = 0
        $stmt = $pdo->prepare("UPDATE bookings SET status = 'Cancelled', queue_position = 0 WHERE booking_id = ?");
        $stmt->execute([$bookingId]);

        // Add Missed Appointment Notification
        $stmt = $pdo->prepare("SELECT center_name FROM service_centers WHERE center_id = ?");
        $stmt->execute([$booking['center_id']]);
        $cName = $stmt->fetchColumn() ?: "Service Center";

        $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, 'Missed Appointment', ?)");
        $msg = "You missed your turn for token " . $booking['token_number'] . " at " . $cName . ". Your booking status has been marked as Cancelled.";
        $stmt->execute([$booking['user_id'], $msg]);

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
        echo json_encode(["success" => true, "message" => "Skipped successfully"]);
        exit;
    }

    if ($action === 'update_entity') {
        $bookingId = (int)$data['bookingId'];
        $status = $data['status'];
        $queuePosition = (int)$data['queuePosition'];

        $stmt = $pdo->prepare("UPDATE bookings SET status = ?, queue_position = ? WHERE booking_id = ?");
        $stmt->execute([$status, $queuePosition, $bookingId]);

        $pdo->commit();
        echo json_encode(["success" => true]);
        exit;
    }

    if ($action === 'delete') {
        $bookingId = (int)$data['bookingId'];
        
        $stmt = $pdo->prepare("DELETE FROM bookings WHERE booking_id = ?");
        $stmt->execute([$bookingId]);

        $pdo->commit();
        echo json_encode(["success" => true]);
        exit;
    }

    $pdo->rollBack();
    echo json_encode(["success" => false, "message" => "Action not supported"]);
} catch (Exception $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
