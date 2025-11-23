<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "tu_clave"; // <--- CAMBIAR
$dbname = "movilesiot";

$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8");
if ($conn->connect_error) { die(json_encode(["status" => "error", "mensaje" => "Error DB"])); }

$accion = isset($_REQUEST['accion']) ? strtoupper($_REQUEST['accion']) : ''; 
$id_usuario = isset($_REQUEST['id_usuario']) ? intval($_REQUEST['id_usuario']) : 0;

if (($accion == 'ABRIR' || $accion == 'CERRAR') && $id_usuario > 0) {
    $tipo_evento = ($accion == 'ABRIR') ? 'APERTURA_MANUAL' : 'CIERRE_MANUAL';
    $fecha = date("Y-m-d H:i:s");

    $sql = "INSERT INTO EVENTOS_ACCESO (id_sensor, id_usuario_ejecutor, tipo_evento, fecha_hora, resultado) 
            VALUES (NULL, $id_usuario, '$tipo_evento', '$fecha', 'PERMITIDO')";

    if ($conn->query($sql) === TRUE) {
        // Generar archivo para NodeMCU (1=Abrir, 0=Cerrar)
        $estado = ($accion == 'ABRIR') ? "1" : "0";
        file_put_contents("estado_barrera.txt", $estado);

        echo json_encode(["status" => "success", "mensaje" => "Barrera " . strtolower($accion) . " correctamente"]);
    } else {
        echo json_encode(["status" => "error", "mensaje" => "Error SQL"]);
    }
} else {
    echo json_encode(["status" => "error", "mensaje" => "Parámetros inválidos"]);
}
$conn->close();
?>