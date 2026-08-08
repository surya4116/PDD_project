<?php
require_once '../../config/database.php';

$data = json_decode(file_get_contents('php://input'), true);

$identifier = '';
if (isset($data['identifier']) && !empty(trim($data['identifier']))) {
    $identifier = trim($data['identifier']);
} elseif (isset($data['phone']) && !empty(trim($data['phone']))) {
    $identifier = trim($data['phone']);
} elseif (isset($data['email']) && !empty(trim($data['email']))) {
    $identifier = trim($data['email']);
}

$password = isset($data['password']) ? $data['password'] : '';

if (empty($identifier) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Phone/Email and password are required"]);
    exit;
}

try {
    $stmt = $pdo->prepare("SELECT * FROM providers WHERE phone = ? OR email = ? LIMIT 1");
    $stmt->execute([$identifier, $identifier]);
    $provider = $stmt->fetch();

    if ($provider) {
        $valid = password_verify($password, $provider['password']) || ($password === $provider['password']);
        if ($valid) {
            $providerData = [
                "id" => (int)$provider['provider_id'],
                "name" => $provider['name'],
                "email" => $provider['email'],
                "phone" => $provider['phone'],
                "shopName" => $provider['shopName'],
                "categoryId" => (int)$provider['categoryId'],
                "location" => $provider['location'],
                "role" => "Provider"
            ];
            echo json_encode([
                "success" => true,
                "message" => "Provider login successful",
                "user" => $providerData,
                "provider" => $providerData
            ]);
            exit;
        }
    }

    echo json_encode(["success" => false, "message" => "Invalid Service Provider credentials"]);

} catch (Exception $e) {
    error_log("Provider Login Error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
