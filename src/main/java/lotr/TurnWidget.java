package lotr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class TurnWidget extends Table {

    private final Game game;

    public TurnWidget(Risk main, GameScreen gameScreen, Game game) {
        this.game = game;

        align(Align.left | Align.top).pad(5);
        columnDefaults(0).expandX().left().uniformX();

        TextButton reinforce = new TextButton("REINFORCE", Risk.skin, "brown");
        reinforce.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ReinforceScreen rsc = new ReinforceScreen(main, game, game.green, gameScreen);
                main.setScreen(rsc);
            }
        });

        add(reinforce).expandX().uniformX().left().minWidth(120);
        row();

        setBounds(0, 250, 300, 250);
        setBackground(new TextureRegionDrawable(Risk.fillRectangle(1, 1, new Color(.5f, .5f, .5f, .5f))));

    }

}
