<?php
$now = time();
$startTs = strtotime('2026-08-06' . ' ' . date('H:i', strtotime('09:00 AM')));
$endTs   = strtotime('2026-08-06' . ' ' . date('H:i', strtotime('11:00 AM')));
echo 'Now: ' . date('Y-m-d H:i:s', $now) . "\n";
echo 'Start: ' . date('Y-m-d H:i:s', $startTs) . "\n";
echo 'End: ' . date('Y-m-d H:i:s', $endTs) . "\n";
if ($now >= $endTs) {
    echo 'Completed';
} elseif ($now >= $startTs) {
    echo 'Running';
} else {
    echo 'Upcoming';
}
?>
