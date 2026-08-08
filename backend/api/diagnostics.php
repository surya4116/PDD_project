<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

$start = microtime(true);

$apiUrl = "http://" . $_SERVER['HTTP_HOST'] . "/smartqueue/api/";
$dbStatus = "failed";
$success = false;

try {
    require_once '../config/database.php';
    if (isset($pdo)) {
        // Quick verification query
        $stmt = $pdo->query("SELECT 1");
        if ($stmt->fetch()) {
            $dbStatus = "connected";
            $success = true;
        }
    }
} catch (Exception $e) {
    $dbStatus = "Database Error: " . $e->getMessage();
}

$end = microtime(true);
$latencyMs = round(($end - $start) * 1000);

echo json_encode([
    "success" => $success,
    "apiUrl" => $apiUrl,
    "apiStatus" => "online",
    "dbStatus" => $dbStatus,
    "latencyMs" => $latencyMs
]);
?>
