package lotr.util;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import java.util.Random;
import lotr.Risk;

public class Dice {

    private static final Model[] blackModels = new Model[6];
    private static final Model[] redModels = new Model[6];

    static {
        for (int i = 0; i < 6; i++) {
            blackModels[i] = buildDiceModel(0, i);
        }
        for (int i = 0; i < 6; i++) {
            redModels[i] = buildDiceModel(1, i);
        }
    }

    private final Random r = new Random();
    private final int num;
    private final int sides;

    public Dice() {
        sides = 6;
        num = 1;
    }

    public int roll() {
        int sum = 0;
        for (int i = 0; i < num; i++) {
            sum += r.nextInt(sides) + 1;
        }
        return sum;
    }

    public static Model getBlackModel(int val) {
        return blackModels[val];
    }

    public static Model getRedModel(int val) {
        return redModels[val];
    }

    private static Model buildDiceModel(int id, int val) {
        ModelBuilder modelBuilder = new ModelBuilder();

        Texture texture = Risk.DICE_TEXTURES[0][0].getTexture();
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        modelBuilder.begin();
        MeshPartBuilder mpb = modelBuilder.part("box", GL20.GL_TRIANGLES, attributes, new Material(TextureAttribute.createDiffuse(texture)));
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 0 ? 3 : 0]);
        mpb.rect(-1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1, 1f, -1f, -1f, 0, 0, -1);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 1 ? 3 : 1]);
        mpb.rect(-1f, 1f, 1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, 0, 0, 1);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 2 ? 3 : 2]);
        mpb.rect(-1f, -1f, 1f, -1f, -1f, -1f, 1f, -1f, -1f, 1f, -1f, 1f, 0, -1, 0);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val]);
        mpb.rect(-1f, 1f, -1f, -1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, -1f, 0, 1, 0);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 4 ? 3 : 4]);
        mpb.rect(-1f, -1f, 1f, -1f, 1f, 1f, -1f, 1f, -1f, -1f, -1f, -1f, -1, 0, 0);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 5 ? 3 : 5]);
        mpb.rect(1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1, 0, 0);
        Model boxModel = modelBuilder.end();
        return boxModel;
    }

}
