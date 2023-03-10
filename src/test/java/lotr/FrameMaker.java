/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lotr;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.testng.annotations.Test;

/**
 *
 * @author panti
 */
public class FrameMaker {

    @Test
    public void makeHUDFrame() throws Exception {

        Color GRAY = new Color(192, 192, 192);
        Color BLACK = new Color(0, 0, 0);

        int w = 300;
        int h = 62;

        BufferedImage output = new BufferedImage(w, h * 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) output.getGraphics();

        Composite defComposite = g2d.getComposite();
        Composite clearComposite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);

        g2d.setColor(BLACK);
        g2d.fillRect(0, 0, w, h * 4);

        g2d.setColor(GRAY);
        g2d.fillRect(0 + 1, 0 * h + 1, w - 2, h - 1);
        g2d.fillRect(0 + 1, 1 * h + 1, w - 2, h - 1);
        g2d.fillRect(0 + 1, 2 * h + 1, w - 2, h - 1);
        g2d.fillRect(0 + 1, 3 * h + 1, w - 2, h - 2);

        g2d.setComposite(clearComposite);
        g2d.setColor(new Color(0, 0, 0, 0));

        g2d.fillRect(0 + 2, 0 * h + 2, w - 4, h - 3);
        g2d.fillRect(0 + 2, 1 * h + 2, w - 4, h - 3);
        g2d.fillRect(0 + 2, 2 * h + 2, w - 4, h - 3);
        g2d.fillRect(0 + 2, 3 * h + 2, w - 4, h - 4);

        g2d.setComposite(defComposite);
        ImageIO.write(output, "PNG", new File("src/main/resources/assets/data/hud-frame.png"));

    }

}
