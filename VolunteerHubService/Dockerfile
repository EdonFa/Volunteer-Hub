# Same Payara version as in the course How-to (1.5): Payara 7.2026.8 Full (it runs Java 21 and also
# accepts our code compiled for Java 17). Every .war copied into $DEPLOY_DIR is deployed when the
# server starts, under the context root /VolunteerHubService (the name of the war).
# Alternative used in the course: deploy the war from IntelliJ to the Payara of the StudyBuddyDocker
# docker-compose (How-to 3.4); see README.md.
FROM payara/server-full:7.2026.8
COPY target/VolunteerHubService.war $DEPLOY_DIR
