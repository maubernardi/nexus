<#ftl output_format="plainText">
<#-- NEXUS: username, data e ora nel fuso di Roma, indirizzo IP -->
<#setting time_zone="Europe/Rome">
${msg("eventUpdateCredentialBody", user.username, event.date?string("dd/MM/yyyy"), event.date?string("HH:mm"), event.ipAddress)}
