<#import "template.ftl" as layout>
<#-- NEXUS: username, data e ora nel fuso di Roma, indirizzo IP -->
<#setting time_zone="Europe/Rome">
<@layout.emailLayout>
${kcSanitize(msg("eventUpdateCredentialBodyHtml", user.username, event.date?string("dd/MM/yyyy"), event.date?string("HH:mm"), event.ipAddress))?no_esc}
</@layout.emailLayout>
