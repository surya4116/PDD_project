<?php
require_once '../../config/database.php';

try {
    $stmt = $pdo->query("SELECT * FROM users ORDER BY id DESC");
    $users = [];
    while ($row = $stmt->fetch()) {
        $users[] = [
            "id" => (int)$row['id'],
            "name" => $row['fullname'],
            "phone" => $row['phone'],
            "email" => $row['email'],
            "isDisabled" => (bool)$row['isDisabled']
        ];
    }
    echo json_encode(["success" => true, "users" => $users]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
