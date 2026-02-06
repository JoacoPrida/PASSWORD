<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Enlaces Útiles</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .boton-gigante {
            padding: 2rem;
            font-size: 1.5rem;
            border-radius: 15px;
            transition: transform 0.2s;
            text-decoration: none;
            display: block;
            color: white;
            font-weight: bold;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .boton-gigante:hover {
            transform: scale(1.02);
            color: white;
            box-shadow: 0 8px 15px rgba(0,0,0,0.2);
        }
    </style>
</head>
<body class="bg-light">

    <nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow mb-5">
        <div class="container">
            <a class="navbar-brand fw-bold" href="lista">
                &#8592; Volver a la Lista
            </a>
            <span class="navbar-text text-white">
                Herramientas Docentes
            </span>
        </div>
    </nav>

    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-8 text-center">
                <h2 class="mb-5 text-secondary">Accesos Rápidos</h2>

                <div class="d-grid gap-4">
                    
                    <a href="https://advice.com.ar/instituciones/instituto-de-ingles-password/?v=c582dec943f" 
                       target="_blank" 
                       class="boton-gigante bg-success">
                        &#128218; ADVICE BOOKSHELF<br>
                        <span style="font-size: 1rem; opacity: 0.9; font-weight: normal;">(Libros Digitales)</span>
                    </a>

                    <a href="https://englishhub.oup.com/myDashboard" 
                       target="_blank" 
                       class="boton-gigante bg-danger">
                        &#127891; OXFORD HUB<br>
                        <span style="font-size: 1rem; opacity: 0.9; font-weight: normal;">(Cursos y Plataforma)</span>
                    </a>

                </div>

                
            </div>
        </div>
    </div>

</body>
</html>