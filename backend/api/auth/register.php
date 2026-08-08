<?php
error_log("Step 1: Request Received");

require_once '../../config/database.php';
error_log("Step 3: Database Connected");

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['name']) || !isset($data['phone']) || !isset($data['email']) || !isset($data['password'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$fullname = trim($data['name']);
$phone = trim($data['phone']);
$email = trim($data['email']);
$password = $data['password'];

if (empty($fullname) || empty($phone) || empty($email) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Fields cannot be empty"]);
    exit;
}

error_log("Step 2: Validation Passed");

try {
    // 1. Specific duplicate email check
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ? LIMIT 1");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        echo json_encode(["success" => false, "message" => "Email is already registered"]);
        exit;
    }

    // 2. Specific duplicate phone check
    $stmt = $pdo->prepare("SELECT id FROM users WHERE phone = ? LIMIT 1");
    $stmt->execute([$phone]);
    if ($stmt->fetch()) {
        echo json_encode(["success" => false, "message" => "Phone number is already registered"]);
        exit;
    }

    // 3. Hash password
    $hashedPassword = password_hash($password, PASSWORD_BCRYPT);

    // 4. Secure insert execution
    $stmt = $pdo->prepare("INSERT INTO users (fullname, email, phone, password) VALUES (?, ?, ?, ?)");
    $stmt->execute([$fullname, $email, $phone, $hashedPassword]);
    $userId = $pdo->lastInsertId();
    
    error_log("Step 4: Insert Success");

    error_log("Step 5: Response Sent");
    echo json_encode([
        "success" => true, 
        "message" => "Registration successful", 
        "userId" => (int)$userId
    ]);
} catch (Exception $e) {
    error_log("Registration SQL Error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "SQL Error: " . $e->getMessage()]);
}
?>
