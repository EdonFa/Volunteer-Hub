# Payara Server 6 = Jakarta EE 10 (same version as the jakartaee-web-api dependency in pom.xml), with Java 17.
# Every .war copied into $DEPLOY_DIR is deployed when the server starts,
# under the context root /VolunteerHubService (the name of the war).
FROM payara/server-full:6.2025.11-jdk17
COPY target/VolunteerHubService.war $DEPLOY_DIR
