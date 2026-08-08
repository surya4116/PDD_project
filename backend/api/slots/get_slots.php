<?php
require_once '../../config/database.php';

$centerId = isset($_GET['centerId']) ? (int)$_GET['centerId'] : null;
$serviceId = isset($_GET['serviceId']) ? (int)$_GET['serviceId'] : null;

try {
    $todayStr = date("Y-m-d");

    if ($serviceId !== null) {
        $stmt = $pdo->prepare("SELECT * FROM slots WHERE serviceId = ? AND date >= ? ORDER BY date ASC, startTime ASC");
        $stmt->execute([$serviceId, $todayStr]);
    } else if ($centerId !== null) {
        $stmt = $pdo->prepare("SELECT * FROM slots WHERE centerId = ? AND date >= ? ORDER BY date ASC, startTime ASC");
        $stmt->execute([$centerId, $todayStr]);
    } else {
        $stmt = $pdo->prepare("SELECT * FROM slots WHERE date >= ? ORDER BY date ASC, startTime ASC");
        $stmt->execute([$todayStr]);
    }

    $slots = $stmt->fetchAll();
    $formatted = [];
    foreach ($slots as $s) {
        $slotStatus = $s['status'];

        if ($s['date'] == $todayStr && $slotStatus != 'Completed' && $slotStatus != 'Cancelled') {
            $now = time();
            $delaySecs = (int)($s['delayMins'] ?? 0) * 60;
            $startTs = strtotime($s['date'] . ' ' . date('H:i', strtotime($s['startTime'])));
            $endTs   = strtotime($s['date'] . ' ' . date('H:i', strtotime($s['endTime']))) + $delaySecs;

            if ($slotStatus === 'Running') {
                if ($now >= ($endTs + 10800)) {
                    $slotStatus = 'Completed';
                }
            } else if ($now >= $endTs) {
                $slotStatus = 'Completed';
            } elseif ($now >= $startTs) {
                $slotStatus = 'Running';
            } else {
                $slotStatus = 'Upcoming';
            }
        }

        $formatted[] = [
            "id"            => (int)$s['id'],
            "serviceId"     => (int)$s['serviceId'],
            "centerId"      => (int)$s['centerId'],
            "date"          => $s['date'],
            "startTime"     => $s['startTime'],
            "endTime"       => $s['endTime'],
            "maxTokens"     => (int)$s['maxTokens'],
            "currentTokens" => (int)$s['currentTokens'],
            "status"        => $slotStatus,
            "delayMins"     => (int)($s['delayMins'] ?? 0)
        ];
    }

    echo json_encode(["success" => true, "slots" => $formatted]);

} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
