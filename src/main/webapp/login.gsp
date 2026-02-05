<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Acceso Restringido</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f0f2f5; display: flex; align-items: center; justify-content: center; height: 100vh; }
        .login-card { max-width: 400px; width: 100%; padding: 2rem; border-radius: 10px; }
    </style>
</head>
<body>
    <div class="card shadow login-card">
        <div class="text-center mb-4">
            <img src="img/logo.jpg" alt="Logo" width="150" class="img-fluid">
            <h4 class="mt-3">Ingreso al Sistema</h4>
        </div>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger text-center">Contraseña incorrecta</div>
        <% } %>

        <form action="login" method="post">
            <div class="mb-3">
                <label class="form-label">Contraseña de Acceso:</label>
                <input type="password" name="password" class="form-control" placeholder="••••••" required>
            </div>
            <div class="d-grid">
                <button type="submit" class="btn btn-primary btn-lg">Entrar</button>
            </div>
        </form>
    </div>
</body>
</html>