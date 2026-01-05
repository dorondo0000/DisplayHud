package kr.dorondo.displayHud.core;

import kr.dorondo.displayHud.DisplayHud;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.TextDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class LayoutCalculator {
    public static Vector3f getLocationVector(Vector3f vector, Vector3f ogscale, Hud hud){
        HudElement element = hud.getElement();
        Vector3f scale = new Vector3f(ogscale);
        double offsetX = 0;
        double offsetY = 0;
        double offsetZ = 0;
        if(element.isComponent()){

            Component component = element.getComponent();
            String text = PlainTextComponentSerializer.plainText().serialize(component);
            offsetY -= scale.y/2;
            int lines = countLinesNl(text);
            offsetY += ((scale.y / 4.0) * (lines - 1.5));
            int lineWidth = hud.getTextLineWidth();
            TextDisplay.TextAlignment textAlignment = hud.getTextAlignment();
            if(textAlignment == TextDisplay.TextAlignment.LEFT){
                offsetX = (scale.x)*(((lineWidth)*0.0125) - 0.5);



            }
            else if(textAlignment == TextDisplay.TextAlignment.RIGHT){
                offsetX = (scale.x)*(((lineWidth)*-0.0125) - 0.55);
                //Bukkit.getLogger().info("오른쪽" + offsetX);
            }
            else{
                scale.x = 0;
            }
            offsetX += scale.x/2;
            offsetY += scale.y/2;
        }
        else if(element.getItemStack().getType().isBlock()){
            offsetZ += scale.z/2;
        }
        else{
            offsetZ += scale.z/32;
        }
        double UnitX = DisplayHud.getInstance().getUnitX();
        double UnitY = DisplayHud.getInstance().getUnitY();
        double ScreenX = DisplayHud.getInstance().getScreenX();
        double ScreenY = DisplayHud.getInstance().getScreenY();
        Integer AlignmentGap = DisplayHud.getInstance().getAlignmentGap();
        double newX = ( (ScreenX/2) - vector.x - offsetX) * UnitX;
        double newY = -1.5*AlignmentGap -0.178+(((ScreenY/2)-(vector.y)-offsetY)*UnitY);
        double newZ = (UnitX*offsetZ)-((vector.z)*100);
        return new Vector3f((float) newX,(float) newY,(float) newZ);
    }

    private static int countLinesNl(String s) {
        return (s == null || s.isEmpty()) ? 1 : s.split("\n").length + 1;
    }

    public static Vector3f getScaleVector(Vector3f scale){
        float unitX = DisplayHud.getInstance().getUnitX();
        float unitY = DisplayHud.getInstance().getUnitY();
        return new Vector3f(scale.x*unitX,scale.y*unitY,scale.z*unitX);
    }

    public static Quaternionf vecToQuat(Vector3f vector){
        Quaternionf quat = new Quaternionf(0,0,0,1);
        float x = (float) Math.toRadians(vector.x);
        float y = (float) Math.toRadians(vector.y);
        float z = (float) Math.toRadians(vector.z);
        quat = quat.rotateZYX(x, y, z);
        return quat;
    }
}
