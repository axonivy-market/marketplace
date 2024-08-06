// Change user/pwd to your requirement
db = db.getSiblingDB('admin');
db.createUser(
    {
        user: "username",
        pwd: "password",
        roles: [
            { role: "userAdminAnyDatabase", db: "admin" },
            { role: "readWriteAnyDatabase", db: "admin" },
            { role: 'root', db: 'admin' }
        ]
    }
)