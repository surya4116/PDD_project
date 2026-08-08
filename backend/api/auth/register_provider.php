<?php
require_once '../../config/database.php';

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['name']) || !isset($data['phone']) || !isset($data['email']) || !isset($data['password']) || !isset($data['shopName']) || !isset($data['categoryId']) || !isset($data['location'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$name       = trim($data['name']);
$phone      = trim($data['phone']);
$email      = trim($data['email']);
$password   = $data['password'];
$shopName   = trim($data['shopName']);
$categoryId = (int)$data['categoryId'];
$location   = trim($data['location']);

if (empty($name) || empty($phone) || empty($email) || empty($password) || empty($shopName) || $categoryId <= 0 || empty($location)) {
    echo json_encode(["success" => false, "message" => "Fields cannot be empty"]);
    exit;
}

// -------------------------------------------------------
// Detect actual column names in live DB
// -------------------------------------------------------

// Find the correct FK column name for service_centers -> providers
$scProviderCol = 'providerId'; // default
try {
    $cols = $pdo->query("SHOW COLUMNS FROM `service_centers`")->fetchAll(PDO::FETCH_COLUMN);
    if (in_array('adminId', $cols) && !in_array('providerId', $cols)) {
        $scProviderCol = 'adminId';
    } elseif (in_array('provider_id', $cols) && !in_array('providerId', $cols)) {
        $scProviderCol = 'provider_id';
    }
} catch (Exception $e) { /* use default */ }

// Find the correct shopName column in providers table
$provShopCol  = 'shopName'; // default
$provCatCol   = 'categoryId'; // default
try {
    $cols = $pdo->query("SHOW COLUMNS FROM `providers`")->fetchAll(PDO::FETCH_COLUMN);
    if (in_array('shop_name', $cols))   $provShopCol = 'shop_name';
    if (in_array('category_id', $cols)) $provCatCol  = 'category_id';
} catch (Exception $e) { /* use default */ }

// -------------------------------------------------------
// Registration
// -------------------------------------------------------
try {
    $pdo->beginTransaction();

    // Check duplicate email
    $stmt = $pdo->prepare("SELECT provider_id FROM providers WHERE email = ? LIMIT 1");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        $pdo->rollBack();
        echo json_encode(["success" => false, "message" => "Service Provider email is already registered"]);
        exit;
    }

    // Check duplicate phone
    $stmt = $pdo->prepare("SELECT provider_id FROM providers WHERE phone = ? LIMIT 1");
    $stmt->execute([$phone]);
    if ($stmt->fetch()) {
        $pdo->rollBack();
        echo json_encode(["success" => false, "message" => "Service Provider phone number is already registered"]);
        exit;
    }

    // Hash password
    $hashedPassword = password_hash($password, PASSWORD_BCRYPT);

    // Insert into providers (use detected column names)
    $sql = "INSERT INTO providers (name, email, phone, password, `$provShopCol`, `$provCatCol`, location) VALUES (?, ?, ?, ?, ?, ?, ?)";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$name, $email, $phone, $hashedPassword, $shopName, $categoryId, $location]);
    $providerId = $pdo->lastInsertId();

    // Insert into service_centers (use detected FK column name)
    $sql2 = "INSERT INTO service_centers (center_name, categoryId, address, phone, image, status, `$scProviderCol`) VALUES (?, ?, ?, ?, ?, ?, ?)";
    $stmt = $pdo->prepare($sql2);
    $stmt->execute([$shopName, $categoryId, $location, $phone, "", "Active", $providerId]);
    $centerId = $pdo->lastInsertId();

    $pdo->commit();

    echo json_encode([
        "success"    => true,
        "message"    => "Service Provider registration successful",
        "providerId" => (int)$providerId,
        "centerId"   => (int)$centerId
    ]);

} catch (Exception $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    error_log("Provider Registration Error: " . $e->getMessage());
    echo json_encode([
        "success" => false,
        "message" => "Registration failed: " . $e->getMessage()
    ]);
}
?>
