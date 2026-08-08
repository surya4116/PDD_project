<?php
require_once '../../config/database.php';
try {
    // 1. Total Users
    $totalUsers = (int)$pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();

    // 2. Total Service Centers
    $totalCenters = (int)$pdo->query("SELECT COUNT(*) FROM service_centers")->fetchColumn();

    // 3. Total Bookings
    $totalBookings = (int)$pdo->query("SELECT COUNT(*) FROM bookings")->fetchColumn();

    // 4. Active Tokens (Waiting, In Premise, Called)
    $activeTokens = (int)$pdo->query("SELECT COUNT(*) FROM bookings WHERE status IN ('Waiting', 'In Premise', 'Called')")->fetchColumn();

    // 5. Completed Services
    $completedServices = (int)$pdo->query("SELECT COUNT(*) FROM bookings WHERE status = 'Completed'")->fetchColumn();

    // 6. Missed Appointments (Cancelled)
    $missedAppointments = (int)$pdo->query("SELECT COUNT(*) FROM bookings WHERE status = 'Cancelled'")->fetchColumn();

    echo json_encode([
        "success" => true,
        "totalUsers" => $totalUsers,
        "totalCenters" => $totalCenters,
        "totalBookings" => $totalBookings,
        "activeTokens" => $activeTokens,
        "completedServices" => $completedServices,
        "missedAppointments" => $missedAppointments
    ]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
