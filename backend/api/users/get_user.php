<?php
require_once '../../config/database.php';

$id = isset($_GET['id']) ? (int)$_GET['id'] : 0;
$role = isset($_GET['role']) ? $_GET['role'] : 'User';

if ($id <= 0) {
    echo json_encode(["success" => false, "message" => "Invalid ID"]);
    exit;
}

try {
    if ($role === 'Admin' || $role === 'Provider') {
        $stmt = $pdo->prepare("SELECT * FROM providers WHERE provider_id = ? LIMIT 1");
        $stmt->execute([$id]);
        $provider = $stmt->fetch();
        if ($provider) {
            echo json_encode([
                "success" => true,
                "user" => [
                    "id" => (int)$provider['provider_id'],
                    "name" => $provider['name'],
                    "phone" => $provider['phone'],
                    "email" => $provider['email'],
                    "shopName" => $provider['shopName'],
                    "categoryId" => (int)$provider['categoryId'],
                    "location" => $provider['location']
                ]
            ]);
        } else {
            echo json_encode(["success" => false, "message" => "Provider not found"]);
        }
    } else {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = ? LIMIT 1");
        $stmt->execute([$id]);
        $user = $stmt->fetch();
        if ($user) {
            echo json_encode([
                "success" => true,
                "user" => [
                    "id" => (int)$user['id'],
                    "name" => $user['fullname'],
                    "phone" => $user['phone'],
                    "email" => $user['email'],
                    "isDisabled" => (bool)$user['isDisabled']
                ]
            ]);
        } else {
            echo json_encode(["success" => false, "message" => "User not found"]);
        }
    }
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
