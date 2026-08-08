<?php
require_once '../config/database.php';

try {
    // 1. Disable foreign keys check
    $pdo->exec("SET FOREIGN_KEY_CHECKS = 0;");

    // 2. Clean categories table and insert the 5 requested categories
    $pdo->exec("TRUNCATE TABLE categories;");
    $stmt = $pdo->prepare("INSERT INTO categories (id, name, iconName, prefix) VALUES (?, ?, ?, ?)");
    $categories = [
        [1, 'Hospital', 'LocalHospital', 'HSP'],
        [2, 'Bank', 'AccountBalance', 'BNK'],
        [3, 'Salon', 'ContentCut', 'SLN'],
        [4, 'Clinics', 'MedicalServices', 'CLN'],
        [5, 'Other Service', 'Category', 'OTH']
    ];
    foreach ($categories as $cat) {
        $stmt->execute($cat);
    }

    // 3. Re-enable foreign keys
    $pdo->exec("SET FOREIGN_KEY_CHECKS = 1;");

    echo json_encode(["success" => true, "message" => "Categories migrated successfully."]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Migration failed: " . $e->getMessage()]);
}
?>
