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
    $stmt = $pdo->prepare("SELECT * FROM users WHERE phone = ? OR email = ? LIMIT 1");
    $stmt->execute([$identifier, $identifier]);
    $user = $stmt->fetch();

    if ($user && (password_verify($password, $user['password']) || $user['password'] === $password)) {
        if (isset($user['isDisabled']) && $user['isDisabled'] == 1) {
            echo json_encode(["success" => false, "message" => "This user account has been disabled by the administrator"]);
            exit;
        }
        echo json_encode([
            "success" => true,
            "message" => "Login successful",
            "user" => [
                "id" => (int)$user['id'],
                "name" => isset($user['fullname']) ? $user['fullname'] : (isset($user['name']) ? $user['name'] : ""),
                "phone" => $user['phone'],
                "email" => $user['email'],
                "isDisabled" => isset($user['isDisabled']) ? (bool)$user['isDisabled'] : false
            ]
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Invalid phone/email or password"]);
    }
} catch (Exception $e) {
    error_log("Login SQL Error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "SQL Error: " . $e->getMessage()]);
}
?>
