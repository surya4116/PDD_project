<?php
require_once '../../config/database.php';
$centerId = isset($_GET['centerId']) ? (int)$_GET['centerId'] : null;
$serviceId = isset($_GET['serviceId']) ? (int)$_GET['serviceId'] : null;

try {
    if ($serviceId !== null) {
        $stmt = $pdo->prepare("SELECT * FROM services WHERE service_id = ?");
        $stmt->execute([$serviceId]);
    } else if ($centerId !== null) {
        $stmt = $pdo->prepare("SELECT * FROM services WHERE center_id = ?");
        $stmt->execute([$centerId]);
    } else {
        echo json_encode(["success" => false, "message" => "Missing parameters"]);
        exit;
    }
    
    $services = $stmt->fetchAll();
    
    $formatted = [];
    foreach ($services as $s) {
        $formatted[] = [
            "id" => (int)$s['service_id'],
            "centerId" => (int)$s['center_id'],
            "name" => $s['name'],
            "duration" => $s['duration']
        ];
    }
    echo json_encode(["success" => true, "services" => $formatted]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
