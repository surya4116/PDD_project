<?php
require_once '../config/database.php';
try {
    $stmt = $pdo->query("SELECT * FROM categories");
    $categories = $stmt->fetchAll();
    
    $formatted = [];
    foreach ($categories as $cat) {
        $formatted[] = [
            "id" => (int)$cat['id'],
            "name" => $cat['name'],
            "iconName" => $cat['iconName'],
            "prefix" => $cat['prefix']
        ];
    }
    echo json_encode(["success" => true, "categories" => $formatted]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
