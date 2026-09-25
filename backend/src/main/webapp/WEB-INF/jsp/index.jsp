<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="cssUrl" value="/css/app.css" />
<c:url var="jsUrl" value="/js/app.js" />
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="">
    <title>Leave desk</title>
    <link rel="stylesheet" href="<c:out value="${cssUrl}" />">
</head>
<body>
<header class="site-header">
    <a class="brand" href="#/leave-requests" aria-label="Leave desk home">LEAVE<span>/</span>DESK</a>
    <nav id="main-nav" aria-label="Main navigation" hidden>
        <a href="#/leave-requests" data-nav="leave-requests">My requests</a>
        <a href="#/approvals" data-nav="approvals">Approvals</a>
        <button id="logout-button" class="button button-quiet" type="button">Sign out</button>
    </nav>
</header>
<main id="app" tabindex="-1"></main>
<div id="live-region" class="visually-hidden" aria-live="polite" aria-atomic="true"></div>

<%@ include file="templates/login.jspf" %>
<%@ include file="templates/leave-requests.jspf" %>
<%@ include file="templates/approvals.jspf" %>
<script type="module" src="<c:out value="${jsUrl}" />"></script>
</body>
</html>