<#-- NEXUS: il layout di Keycloak con la firma in fondo a ogni email HTML -->
<#macro emailLayout>
<html lang="${locale.language}" dir="${(ltr)?then('ltr','rtl')}">
<body>
    <#nested>
    <p>Portale Nexus</p>
</body>
</html>
</#macro>
