<?php
require_once '../../config/database.php';

header('Content-Type: application/json');

try {
    // Get exact column info for service_centers
    $stmt = $pdo->query("SHOW COLUMNS FROM `service_centers`");
    $sc_cols = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Get exact column info for providers
    $stmt = $pdo->query("SHOW COLUMNS FROM `providers`");
    $p_cols = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Try a test INSERT to see what error occurs
    $testError = null;
    try {
        $pdo->exec("INSERT INTO service_centers (center_name, categoryId, address, phone, image, status, providerId) VALUES ('TEST', 1, 'TEST', '0000000000', '', 'Active', 1)");
        $pdo->exec("DELETE FROM service_centers WHERE center_name = 'TEST'");
        $testError = "providerId INSERT: SUCCESS";
    } catch (Exception $e) {
        $testError = "providerId INSERT failed: " . $e->getMessage();
        // Try with adminId
        try {
            $pdo->exec("INSERT INTO service_centers (center_name, categoryId, address, phone, image, status, adminId) VALUES ('TEST', 1, 'TEST', '0000000000', '', 'Active', 1)");
            $pdo->exec("DELETE FROM service_centers WHERE center_name = 'TEST'");
            $testError .= " | adminId INSERT: SUCCESS";
        } catch (Exception $e2) {
            $testError .= " | adminId INSERT also failed: " . $e2->getMessage();
        }
    }

    echo json_encode([
        "service_centers_columns" => array_column($sc_cols, 'Field'),
        "providers_columns" => array_column($p_cols, 'Field'),
        "insert_test" => $testError
    ], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>
