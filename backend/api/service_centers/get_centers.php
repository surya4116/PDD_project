<?php
require_once '../../config/database.php';

$categoryId = isset($_GET['categoryId']) ? (int)$_GET['categoryId'] : null;
$query = isset($_GET['query']) ? $_GET['query'] : null;
$providerId = isset($_GET['providerId']) ? (int)$_GET['providerId'] : null;

// Also support legacy 'adminId' param for backward compatibility
if ($providerId === null && isset($_GET['adminId'])) {
    $providerId = (int)$_GET['adminId'];
}

try {
    if ($providerId !== null) {
        $stmt = $pdo->prepare("SELECT * FROM service_centers WHERE providerId = ?");
        $stmt->execute([$providerId]);
    } else if ($query !== null && trim($query) !== '') {
        $stmt = $pdo->prepare("SELECT * FROM service_centers WHERE (center_name LIKE ? OR address LIKE ?) AND status = 'Active'");
        $stmt->execute(["%$query%", "%$query%"]);
    } else if ($categoryId !== null) {
        $stmt = $pdo->prepare("SELECT * FROM service_centers WHERE categoryId = ? AND status = 'Active'");
        $stmt->execute([$categoryId]);
    } else {
        $stmt = $pdo->query("SELECT * FROM service_centers WHERE status = 'Active'");
    }
    
    $centers = $stmt->fetchAll();
    
    // Fetch all existing valid provider IDs to exclude orphan service centers of deleted providers
    $validProviderIds = [];
    try {
        $cols = $pdo->query("SHOW COLUMNS FROM `providers`")->fetchAll(PDO::FETCH_COLUMN);
        $provPk = in_array('provider_id', $cols) ? 'provider_id' : 'id';
        $validProviderIds = $pdo->query("SELECT `$provPk` FROM providers")->fetchAll(PDO::FETCH_COLUMN);
        $validProviderIds = array_map('intval', $validProviderIds);
    } catch (Exception $e) {}

    $formatted = [];
    foreach ($centers as $c) {
        $pId = isset($c['providerId']) ? (int)$c['providerId'] : (isset($c['adminId']) ? (int)$c['adminId'] : 0);
        
        // Skip centers if the provider has been deleted from providers table
        if (!empty($validProviderIds) && !in_array($pId, $validProviderIds)) {
            continue;
        }

        $formatted[] = [
            "id" => (int)$c['center_id'],
            "name" => $c['center_name'],
            "categoryId" => (int)$c['categoryId'],
            "address" => $c['address'],
            "providerId" => $pId,
            "isActive" => $c['status'] === 'Active'
        ];
    }
    
    echo json_encode(["success" => true, "centers" => $formatted]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
