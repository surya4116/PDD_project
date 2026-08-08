<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);
if (!isset($data['serviceId']) || !isset($data['centerId']) || !isset($data['date']) || !isset($data['startTime']) || !isset($data['endTime']) || !isset($data['maxTokens'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}
try {
    $today = date("Y-m-d");
    if ($data['date'] < $today) {
        echo json_encode(["success" => false, "message" => "Cannot create slots for past dates."]);
        exit;
    }

    $stmt = $pdo->prepare("INSERT INTO slots (serviceId, centerId, date, startTime, endTime, maxTokens, currentTokens, status, delayMins) VALUES (?, ?, ?, ?, ?, ?, 0, 'Upcoming', 0)");
    $stmt->execute([
        (int)$data['serviceId'],
        (int)$data['centerId'],
        $data['date'],
        $data['startTime'],
        $data['endTime'],
        (int)$data['maxTokens']
    ]);
    echo json_encode(["success" => true, "id" => (int)$pdo->lastInsertId()]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
