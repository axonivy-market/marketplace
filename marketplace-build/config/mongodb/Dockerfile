# Use the base image for MongoDB
FROM mongodb/mongodb-community-server:7.0.0-ubi8

# Copy the custom MongoDB configuration file into the container
COPY mongod.conf /etc/mongod.conf

# Expose MongoDB port
EXPOSE 27017

# Define the default command to run MongoDB with the custom configuration
CMD ["mongod", "--config", "/etc/mongod.conf"]
