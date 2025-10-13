/**
 * Reads every outfit icon sprite,
 * and generates an "item/generated" model for it
 * academy:item/outfit/id.png -> academy:item/outfit/id.json
 */

import fs from "node:fs";
import path from "node:path";

const iconsDir = path.resolve(
  __dirname,
  "../../common/src/main/resources/assets/academy/textures/item/outfit"
);

const modelsDir = path.resolve(
  __dirname,
  "../../common/src/main/resources/assets/academy/models/item/outfit"
);

fs.readdir(iconsDir, (err, files) => {
  Promise.all(
    files
      .filter((file) => file.endsWith(".png"))
      .map((file) => path.basename(file, ".png"))
      .map((id) => generateModelFile(id))
  );
});

async function generateModelFile(id: string) {
  const outFile = path.resolve(modelsDir, `./${id}.json`);
  console.log("Writing =>", outFile);
  fs.writeFileSync(
    outFile,
    `{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "academy:item/outfit/${id}"
  }
}
`
  );
}
