<?php
require_once '../../config/database.php';

$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;
$providerId = isset($_GET['providerId']) ? (int)$_GET['providerId'] : null;
// Legacy support for adminId query param
if ($providerId === null && isset($_GET['adminId'])) {
    $providerId = (int)$_GET['adminId'];
}
$slotId = isset($_GET['slotId']) ? (int)$_GET['slotId'] : null;
$bookingId = isset($_GET['bookingId']) ? (int)$_GET['bookingId'] : null;

try {
    if ($bookingId !== null) {
        $stmt = $pdo->prepare("
            SELECT b.*, s.id as slot_id, s.date, s.startTime, s.endTime, s.maxTokens, s.currentTokens, s.status as slot_status, s.delayMins,
                   c.center_id, c.center_name, c.categoryId, c.address, c.providerId as center_provider,
                   t.token_id, t.estimated_wait_time_mins, t.issued_at,
                   u.fullname as user_name
            FROM bookings b
            JOIN slots s ON b.slot_id = s.id
            JOIN service_centers c ON b.center_id = c.center_id
            LEFT JOIN tokens t ON b.booking_id = t.booking_id
            LEFT JOIN users u ON b.user_id = u.id
            WHERE b.booking_id = ?
            LIMIT 1
        ");
        $stmt->execute([$bookingId]);
    } else if ($userId !== null) {
        $stmt = $pdo->prepare("
            SELECT b.*, s.id as slot_id, s.date, s.startTime, s.endTime, s.maxTokens, s.currentTokens, s.status as slot_status, s.delayMins,
                   c.center_id, c.center_name, c.categoryId, c.address, c.providerId as center_provider,
                   t.token_id, t.estimated_wait_time_mins, t.issued_at,
                   u.fullname as user_name
            FROM bookings b
            JOIN slots s ON b.slot_id = s.id
            JOIN service_centers c ON b.center_id = c.center_id
            LEFT JOIN tokens t ON b.booking_id = t.booking_id
            LEFT JOIN users u ON b.user_id = u.id
            WHERE b.user_id = ?
            ORDER BY b.booking_time DESC
        ");
        $stmt->execute([$userId]);
    } else if ($providerId !== null) {
        $stmt = $pdo->prepare("
            SELECT b.*, s.id as slot_id, s.date, s.startTime, s.endTime, s.maxTokens, s.currentTokens, s.status as slot_status, s.delayMins,
                   c.center_id, c.center_name, c.categoryId, c.address, c.providerId as center_provider,
                   t.token_id, t.estimated_wait_time_mins, t.issued_at,
                   u.fullname as user_name
            FROM bookings b
            JOIN slots s ON b.slot_id = s.id
            JOIN service_centers c ON b.center_id = c.center_id
            LEFT JOIN tokens t ON b.booking_id = t.booking_id
            LEFT JOIN users u ON b.user_id = u.id
            WHERE c.providerId = ?
            ORDER BY b.booking_time DESC
        ");
        $stmt->execute([$providerId]);
    } else if ($slotId !== null) {
        $stmt = $pdo->prepare("
            SELECT b.*, s.id as slot_id, s.date, s.startTime, s.endTime, s.maxTokens, s.currentTokens, s.status as slot_status, s.delayMins,
                   c.center_id, c.center_name, c.categoryId, c.address, c.providerId as center_provider,
                   t.token_id, t.estimated_wait_time_mins, t.issued_at,
                   u.fullname as user_name
            FROM bookings b
            JOIN slots s ON b.slot_id = s.id
            JOIN service_centers c ON b.center_id = c.center_id
            LEFT JOIN tokens t ON b.booking_id = t.booking_id
            LEFT JOIN users u ON b.user_id = u.id
            WHERE b.slot_id = ?
            ORDER BY b.queue_position ASC
        ");
        $stmt->execute([$slotId]);
    } else {
        $stmt = $pdo->query("
            SELECT b.*, s.id as slot_id, s.date, s.startTime, s.endTime, s.maxTokens, s.currentTokens, s.status as slot_status, s.delayMins,
                   c.center_id, c.center_name, c.categoryId, c.address, c.providerId as center_provider,
                   t.token_id, t.estimated_wait_time_mins, t.issued_at,
                   u.fullname as user_name
            FROM bookings b
            JOIN slots s ON b.slot_id = s.id
            JOIN service_centers c ON b.center_id = c.center_id
            LEFT JOIN tokens t ON b.booking_id = t.booking_id
            LEFT JOIN users u ON b.user_id = u.id
            ORDER BY b.booking_time DESC
        ");
    }
    
    $rows = $stmt->fetchAll();
    
    $bookings = [];
    foreach ($rows as $r) {
        $bookings[] = [
            "booking" => [
                "id" => (int)$r['booking_id'],
                "userId" => (int)$r['user_id'],
                "userName" => $r['user_name'] ?? 'Unknown User',
                "slotId" => (int)$r['slot_id'],
                "centerId" => (int)$r['center_id'],
                "tokenNumber" => $r['token_number'],
                "queuePosition" => (int)$r['queue_position'],
                "status" => $r['status'],
                "bookingTime" => (float)$r['booking_time']
            ],
            "slot" => [
                "id" => (int)$r['slot_id'],
                "serviceId" => 0,
                "centerId" => (int)$r['center_id'],
                "date" => $r['date'],
                "startTime" => $r['startTime'],
                "endTime" => $r['endTime'],
                "maxTokens" => (int)$r['maxTokens'],
                "currentTokens" => (int)$r['currentTokens'],
                "status" => $r['slot_status'],
                "delayMins" => (int)$r['delayMins']
            ],
            "center" => [
                "id" => (int)$r['center_id'],
                "name" => $r['center_name'],
                "categoryId" => (int)$r['categoryId'],
                "address" => $r['address'],
                "providerId" => (int)$r['center_provider'],
                "isActive" => true
            ],
            "queueToken" => $r['token_id'] ? [
                "id" => (int)$r['token_id'],
                "bookingId" => (int)$r['booking_id'],
                "tokenNumber" => $r['token_number'],
                "estimatedWaitTimeMins" => (int)$r['estimated_wait_time_mins'],
                "issuedAt" => (float)$r['issued_at']
            ] : null
        ];
    }
    
    echo json_encode(["success" => true, "bookings" => $bookings]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
