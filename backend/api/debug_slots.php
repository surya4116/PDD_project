<?php
try {
    $pdo = new PDO('mysql:host=localhost;dbname=smartqueue_db', 'root', '');
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    
    echo "=== SLOTS TABLE SCHEMA ===\n";
    $stmt = $pdo->query('DESCRIBE slots');
    while($r = $stmt->fetch()) {
        echo $r['Field'] . ': ' . $r['Type'] . "\n";
    }
    
    echo "\n=== SAMPLE SLOTS DATA ===\n";
    $stmt = $pdo->query('SELECT * FROM slots LIMIT 5');
    while($r = $stmt->fetch()) {
        echo json_encode($r) . "\n";
    }
    
    echo "\n=== SERVICES TABLE SCHEMA ===\n";
    $stmt = $pdo->query('DESCRIBE services');
    while($r = $stmt->fetch()) {
        echo $r['Field'] . ': ' . $r['Type'] . "\n";
    }
    
    echo "\n=== SAMPLE SERVICES ===\n";
    $stmt = $pdo->query('SELECT * FROM services LIMIT 5');
    while($r = $stmt->fetch()) {
        echo json_encode($r) . "\n";
    }
} catch(Exception $e) {
    echo "ERROR: " . $e->getMessage();
}
?>
