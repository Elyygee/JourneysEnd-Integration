package abeshutt.staracademy.screen.helper;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class Texture9SliceRegion {

    protected int u, v;
    protected int regionW, regionH;
    protected int texW, texH;

    public Texture9SliceRegion(int u, int v, int regionW, int regionH, int texW, int textH) {
        this.u = u;
        this.v = v;
        this.regionW = regionW;
        this.regionH = regionH;
        this.texW = texW;
        this.texH = textH;
    }

    public void draw(DrawContext context, Identifier texture,
                     int x, int y,
                     int width, int height) {
        int cornerWidth = (this.regionW - 1) / 2;
        int cornerHeight = (this.regionH - 1) / 2;

        // Corners
        context.drawTexture(texture, x, y, cornerWidth, cornerHeight, this.u, this.v, cornerWidth, cornerHeight, this.texW, this.texH);
        context.drawTexture(texture, x + width - cornerWidth - 1, y, cornerWidth, cornerHeight, this.u + cornerWidth + 2, v, cornerWidth, cornerHeight, this.texW, this.texH);
        context.drawTexture(texture, x + width - cornerWidth - 1, y + height - cornerHeight - 1, cornerWidth, cornerHeight, this.u + cornerWidth + 2, this.v + cornerHeight + 2, cornerWidth, cornerHeight, this.texW, this.texH);
        context.drawTexture(texture, x, y + height - cornerHeight - 1, cornerWidth, cornerHeight, this.u, this.v + cornerHeight + 2, cornerWidth, cornerHeight, this.texW, this.texH);

        // Edges
        context.drawTexture(texture, x + cornerWidth - 1, y, width - 2 * cornerWidth, cornerHeight, u + cornerWidth, v, 1, cornerHeight, this.texW, this.texH);
        context.drawTexture(texture, x, y + cornerHeight - 1, cornerWidth, height - 2 * cornerHeight, u, v + cornerHeight, cornerWidth, 1, this.texW, this.texH);
        context.drawTexture(texture, x + width - cornerWidth - 2, y + cornerHeight - 1, cornerWidth, height - 2 * cornerHeight, u + cornerWidth + 1, v + cornerHeight, cornerWidth, 1, texW, texH);
        context.drawTexture(texture, x + cornerWidth - 1, y + height - cornerHeight - 1, width - 2 * cornerWidth, cornerHeight, u + cornerWidth, v + cornerHeight + 2, 1, cornerHeight, texW, texH);

        // Center
        context.drawTexture(texture, x + cornerWidth - 1, y + cornerHeight - 1, width - 2 * cornerWidth, height - 2 * cornerHeight, u + cornerWidth, v + cornerHeight, 1, 1, texW, texH);
    }

}
