const fse = require("fs-extra");

const input = "build"
const staticOutput = "../../../target/classes/static"

console.log("Removing static directory")
fse.removeSync(staticOutput);

fse.ensureDirSync(input);
fse.ensureDirSync(staticOutput);

console.log("Copying build arifacts to static directory")
fse.copySync(input, staticOutput);

console.log("Installed frontend")
