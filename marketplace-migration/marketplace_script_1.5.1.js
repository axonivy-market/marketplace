function moveProductFieldsAndCleanUp() {
  const products = db.getCollection("Product").find({}, {
    _id: 1,
    installationCount: 1,
    synchronizedInstallationCount: 1,
    customOrder: 1
  }).toArray();

  if (products.length > 0) {
    db.getCollection("ProductMarketplaceData").insertMany(products);
    print("Fields successfully moved to ProductMarketplaceData.");
  } else {
    print("No fields to move.");
  }

  const result = db.getCollection("Product").updateMany(
    {},
    { $unset: { installationCount: "", synchronizedInstallationCount: "", customOrder: "" } }
  );
  print(`Fields removed from ${result.modifiedCount} documents in the Product collection.`);
}

moveProductFieldsAndCleanUp();